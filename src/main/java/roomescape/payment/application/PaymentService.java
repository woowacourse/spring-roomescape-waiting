package roomescape.payment.application;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentOrderRepository;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.domain.PaymentStatus;
import roomescape.payment.domain.exception.PaymentAlreadyProcessedException;
import roomescape.payment.domain.exception.PaymentAmountMismatchException;
import roomescape.payment.domain.exception.PaymentGatewayException;
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

    private static final String ORDER_NAME = "방탈출 예약";

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentGateway paymentGateway;
    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ReservationHistoryService reservationHistoryService;
    private final Long reservationAmount;

    public PaymentService(
            PaymentOrderRepository paymentOrderRepository,
            PaymentGateway paymentGateway,
            ReservationDao reservationDao,
            ReservationTimeDao reservationTimeDao,
            ReservationHistoryService reservationHistoryService,
            @Value("${payment.reservation-amount}") Long reservationAmount
    ) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentGateway = paymentGateway;
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.reservationHistoryService = reservationHistoryService;
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

    @Transactional
    public PaymentResult confirm(Long memberId, String paymentKey, String orderId, Long amount) {
        PaymentOrder order = findPaymentOrder(orderId);
        Reservation reservation = findReservation(order.getReservationId());
        validateOwner(reservation, memberId);
        validateAmount(order, amount);

        if (order.isDone()) {
            if (order.getPaymentKey().equals(paymentKey)) {
                return new PaymentResult(paymentKey, orderId, PaymentStatus.DONE, order.getAmount());
            }
            throw new PaymentAlreadyProcessedException();
        }

        PaymentResult result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));
        validateResult(order, paymentKey, result);

        paymentOrderRepository.update(order.complete(result.paymentKey()));
        Reservation confirmed = reservation.confirm();
        reservationDao.update(confirmed);
        reservationHistoryService.recordCreated(confirmed, memberId);
        return result;
    }

    @Transactional
    public void fail(Long memberId, String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return;
        }
        PaymentOrder order = paymentOrderRepository.findByOrderId(orderId).orElse(null);
        if (order == null || order.isDone()) {
            return;
        }
        Reservation reservation = findReservation(order.getReservationId());
        validateOwner(reservation, memberId);
        paymentOrderRepository.deleteByOrderId(orderId);
        reservationDao.delete(reservation.getId());
    }

    private void validateOwner(Reservation reservation, Long memberId) {
        if (!reservation.isReservedBy(memberId)) {
            throw new PaymentOwnerMismatchException();
        }
    }

    private void validateAmount(PaymentOrder order, Long amount) {
        if (!order.getAmount().equals(amount)) {
            throw new PaymentAmountMismatchException();
        }
    }

    private void validateResult(PaymentOrder order, String paymentKey, PaymentResult result) {
        if (result == null
                || result.status() != PaymentStatus.DONE
                || !order.getOrderId().equals(result.orderId())
                || !order.getAmount().equals(result.approvedAmount())
                || !paymentKey.equals(result.paymentKey())) {
            throw new PaymentGatewayException();
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
}
