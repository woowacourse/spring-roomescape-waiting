package roomescape.payment.application;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentOrderRepository;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.domain.PaymentStatus;
import roomescape.payment.domain.exception.PaymentAlreadyProcessedException;
import roomescape.payment.domain.exception.PaymentGatewayResponseTimeoutException;
import roomescape.payment.domain.exception.PaymentNotFoundException;
import roomescape.payment.domain.exception.PaymentOwnerMismatchException;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationDao;
import roomescape.reservation.exception.ReservationAlreadyExistsException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservationhistory.ReservationHistoryService;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.ReservationTimeDao;
import roomescape.reservationtime.exception.ReservationTimeNotFoundException;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final String ORDER_NAME = "방탈출 예약";
    private static final int MAX_GATEWAY_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 500L;

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentGateway paymentGateway;
    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ReservationHistoryService reservationHistoryService;
    private final TransactionTemplate transactionTemplate;
    private final Long reservationAmount;

    public PaymentService(
            PaymentOrderRepository paymentOrderRepository,
            PaymentGateway paymentGateway,
            ReservationDao reservationDao,
            ReservationTimeDao reservationTimeDao,
            ReservationHistoryService reservationHistoryService,
            TransactionTemplate transactionTemplate,
            @Value("${payment.reservation-amount}") Long reservationAmount
    ) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentGateway = paymentGateway;
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.reservationHistoryService = reservationHistoryService;
        this.transactionTemplate = transactionTemplate;
        this.reservationAmount = reservationAmount;
    }

    @Transactional
    public PaymentCheckout prepareReservation(
            Long memberId,
            LocalDate date,
            Long timeId,
            Long themeId,
            Long storeId
    ) {
        ReservationTime time = findReservationTime(timeId);
        Reservation pending = Reservation.createPending(memberId, date, time, themeId, storeId);
        Reservation saved;
        try {
            saved = reservationDao.insert(pending);
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistsException();
        }

        String orderId = generateOrderId();
        paymentOrderRepository.insert(PaymentOrder.ready(orderId, saved.getId(), reservationAmount));
        return new PaymentCheckout(saved, orderId, ORDER_NAME, reservationAmount);
    }

    public PaymentResult confirm(Long memberId, String paymentKey, String orderId, Long amount) {
        Prepared prepared = transactionTemplate.execute(s -> prepareConfirm(memberId, paymentKey, orderId, amount));
        if (prepared.shortCircuit() != null) {
            return prepared.shortCircuit();
        }

        try {
            PaymentResult result = retryOnTimeout(() ->
                    paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount)));
            return transactionTemplate.execute(s -> finalizeConfirm(orderId, paymentKey, memberId, result));
        } catch (PaymentGatewayResponseTimeoutException e) {
            transactionTemplate.executeWithoutResult(s -> markUnconfirmed(orderId, paymentKey));
            throw e;
        }
    }

    @Transactional
    public void fail(Long memberId, String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return;
        }
        PaymentOrder order = paymentOrderRepository.findByOrderId(orderId).orElse(null);
        if (order == null || order.isDone() || order.isUnconfirmed()) {
            return;
        }
        Reservation reservation = findReservation(order.getReservationId());
        validateOwner(reservation, memberId);
        paymentOrderRepository.deleteByOrderId(orderId);
        reservationDao.delete(reservation.getId());
    }

    private Prepared prepareConfirm(Long memberId, String paymentKey, String orderId, Long amount) {
        PaymentOrder order = findPaymentOrder(orderId);
        Reservation reservation = findReservation(order.getReservationId());
        validateOwner(reservation, memberId);
        order.requireAmount(amount);

        if (order.isDone()) {
            if (order.getPaymentKey().equals(paymentKey)) {
                return Prepared.shortCircuit(
                        new PaymentResult(paymentKey, orderId, PaymentStatus.DONE, order.getAmount()));
            }
            throw new PaymentAlreadyProcessedException();
        }
        if (order.isUnconfirmed() && !order.getPaymentKey().equals(paymentKey)) {
            throw new PaymentAlreadyProcessedException();
        }
        return Prepared.proceed();
    }

    private PaymentResult finalizeConfirm(String orderId, String paymentKey, Long memberId, PaymentResult result) {
        PaymentOrder order = findPaymentOrder(orderId);
        order.requireMatchingResult(paymentKey, result);

        if (order.isDone()) {
            return result;
        }

        paymentOrderRepository.update(order.confirmWith(result.paymentKey()));

        Reservation reservation = findReservation(order.getReservationId());
        if (!reservation.isConfirmed()) {
            Reservation confirmed = reservation.confirm();
            reservationDao.update(confirmed);
            reservationHistoryService.recordCreated(confirmed, memberId);
        }
        return result;
    }

    private void markUnconfirmed(String orderId, String paymentKey) {
        PaymentOrder order = paymentOrderRepository.findByOrderIdForUpdate(orderId)
                .orElseThrow(PaymentNotFoundException::new);
        if (order.isDone() || order.isUnconfirmed()) {
            return;
        }
        paymentOrderRepository.update(order.markUnconfirmed(paymentKey));
        log.warn("결제 결과 불명확 — UNCONFIRMED 마킹. orderId={}", orderId);
    }

    private <T> T retryOnTimeout(Supplier<T> call) {
        int attempts = 0;
        long backoffMs = INITIAL_BACKOFF_MS;
        while (true) {
            try {
                return call.get();
            } catch (PaymentGatewayResponseTimeoutException e) {
                attempts++;
                if (attempts >= MAX_GATEWAY_ATTEMPTS) {
                    throw e;
                }
                log.warn("결제 게이트웨이 응답 타임아웃 — {}회 재시도 예정", attempts);
                sleep(backoffMs);
                backoffMs *= 2;
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentGatewayResponseTimeoutException();
        }
    }

    private void validateOwner(Reservation reservation, Long memberId) {
        if (!reservation.isReservedBy(memberId)) {
            throw new PaymentOwnerMismatchException();
        }
    }

    private PaymentOrder findPaymentOrder(String orderId) {
        return paymentOrderRepository.findByOrderIdForUpdate(orderId)
                .orElseThrow(PaymentNotFoundException::new);
    }

    private Reservation findReservation(Long reservationId) {
        try {
            return reservationDao.findReservationByIdForUpdate(reservationId);
        } catch (EmptyResultDataAccessException e) {
            throw new ReservationNotFoundException();
        }
    }

    private ReservationTime findReservationTime(Long timeId) {
        try {
            return reservationTimeDao.findReservationTimeById(timeId);
        } catch (EmptyResultDataAccessException e) {
            throw new ReservationTimeNotFoundException();
        }
    }

    private String generateOrderId() {
        return "order-" + UUID.randomUUID().toString().replace("-", "");
    }

    private record Prepared(PaymentResult shortCircuit) {
        static Prepared shortCircuit(PaymentResult result) {
            return new Prepared(result);
        }

        static Prepared proceed() {
            return new Prepared(null);
        }
    }
}
