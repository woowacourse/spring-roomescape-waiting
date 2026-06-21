package roomescape.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.InvalidRequestException;
import roomescape.payment.config.PaymentProperties;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentOrderDetails;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.domain.exception.PaymentAlreadyProcessedException;
import roomescape.payment.domain.exception.PaymentAmountMismatchException;
import roomescape.payment.domain.exception.PaymentConfirmationPendingException;
import roomescape.payment.domain.exception.PaymentGatewayException;
import roomescape.payment.domain.exception.PaymentInvalidRequestException;
import roomescape.payment.repository.PaymentOrderRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.ReservationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class PaymentService {
    private static final String ORDER_ID_PREFIX = "RESV_";

    private final PaymentOrderRepository paymentOrderRepository;
    private final ReservationService reservationService;
    private final PaymentGateway paymentGateway;
    private final PaymentProperties paymentProperties;

    public PaymentService(
            PaymentOrderRepository paymentOrderRepository,
            ReservationService reservationService,
            PaymentGateway paymentGateway,
            PaymentProperties paymentProperties
    ) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.reservationService = reservationService;
        this.paymentGateway = paymentGateway;
        this.paymentProperties = paymentProperties;
    }

    @Transactional
    public PaymentReadyOrder prepare(String name, LocalDate date, Long timeId, Long themeId) {
        reservationService.validateCreatable(name, date, timeId, themeId);

        PaymentOrder paymentOrder = PaymentOrder.ready(
                generateOrderId(),
                generateIdempotencyKey(),
                paymentProperties.reservationAmount(),
                name,
                date,
                timeId,
                themeId,
                LocalDateTime.now()
        );

        return PaymentReadyOrder.from(paymentOrderRepository.save(paymentOrder));
    }

    @Transactional(noRollbackFor = PaymentConfirmationPendingException.class)
    public Reservation confirm(String paymentKey, String orderId, long amount) {
        PaymentOrder paymentOrder = findOrder(orderId);

        if (!paymentOrder.hasAmount(amount)) {
            throw new PaymentAmountMismatchException("결제 금액이 주문 금액과 일치하지 않습니다.");
        }
        if (paymentOrder.isConfirmed()) {
            return confirmIdempotently(paymentOrder, paymentKey);
        }
        if (!paymentOrder.isConfirmable()) {
            throw new PaymentInvalidRequestException("승인 확인이 가능한 주문이 아닙니다.");
        }

        reservationService.validateCreatable(
                paymentOrder.getName(),
                paymentOrder.getDate(),
                paymentOrder.getTimeId(),
                paymentOrder.getThemeId()
        );

        PaymentResult paymentResult;
        try {
            paymentResult = paymentGateway.confirm(new PaymentConfirmation(
                    paymentKey,
                    orderId,
                    amount,
                    paymentOrder.getIdempotencyKey()
            ));
        } catch (PaymentConfirmationPendingException exception) {
            paymentOrderRepository.update(paymentOrder.pendingConfirmation(LocalDateTime.now()));
            throw exception;
        } catch (PaymentAlreadyProcessedException exception) {
            return confirmAlreadyProcessedOrder(orderId, paymentKey, amount, exception);
        }
        validatePaymentResult(paymentResult, paymentKey, orderId, amount);

        Reservation reservation = reservationService.create(
                paymentOrder.getName(),
                paymentOrder.getDate(),
                paymentOrder.getTimeId(),
                paymentOrder.getThemeId()
        );

        paymentOrderRepository.update(paymentOrder.confirm(
                paymentResult.paymentKey(),
                reservation.getId(),
                LocalDateTime.now()
        ));

        return reservation;
    }

    private Reservation confirmAlreadyProcessedOrder(
            String orderId,
            String paymentKey,
            long amount,
            PaymentAlreadyProcessedException exception
    ) {
        PaymentOrder refreshedOrder = findOrder(orderId);
        if (refreshedOrder.isConfirmed() && refreshedOrder.hasAmount(amount)) {
            return confirmIdempotently(refreshedOrder, paymentKey);
        }

        throw exception;
    }

    private Reservation confirmIdempotently(PaymentOrder paymentOrder, String paymentKey) {
        if (!Objects.equals(paymentOrder.getPaymentKey(), paymentKey)) {
            throw new PaymentAlreadyProcessedException("이미 다른 결제 키로 승인된 결제입니다.");
        }
        if (paymentOrder.getReservationId() == null) {
            throw new PaymentGatewayException("이미 승인된 결제 주문의 예약 정보가 없습니다.");
        }

        return reservationService.findById(paymentOrder.getReservationId());
    }

    private void validatePaymentResult(PaymentResult paymentResult, String paymentKey, String orderId, long amount) {
        if (!Objects.equals(paymentResult.paymentKey(), paymentKey)) {
            throw new PaymentGatewayException("결제 승인 응답 키가 요청 키와 일치하지 않습니다.");
        }
        if (!Objects.equals(paymentResult.orderId(), orderId)) {
            throw new PaymentGatewayException("결제 승인 응답 주문번호가 요청 주문번호와 일치하지 않습니다.");
        }
        if (paymentResult.amount() != amount) {
            throw new PaymentGatewayException("결제 승인 응답 금액이 요청 금액과 일치하지 않습니다.");
        }
    }

    @Transactional
    public void fail(String orderId, String code, String message) {
        if (orderId == null || orderId.isBlank()) {
            return;
        }

        paymentOrderRepository.findByOrderId(orderId)
                .filter(PaymentOrder::isReady)
                .map(paymentOrder -> paymentOrder.fail(code, message, LocalDateTime.now()))
                .ifPresent(paymentOrderRepository::update);
    }

    @Transactional(readOnly = true)
    public List<PaymentOrderDetails> findOrdersByName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException("조회할 이름을 입력해 주세요.");
        }

        return paymentOrderRepository.findDetailsByName(name.trim());
    }

    private PaymentOrder findOrder(String orderId) {
        return paymentOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentInvalidRequestException("결제 주문을 찾을 수 없습니다."));
    }

    private String generateOrderId() {
        return ORDER_ID_PREFIX + UUID.randomUUID();
    }

    private String generateIdempotencyKey() {
        return UUID.randomUUID().toString();
    }
}
