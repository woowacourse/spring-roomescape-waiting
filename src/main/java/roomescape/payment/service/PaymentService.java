package roomescape.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.payment.config.PaymentProperties;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.domain.exception.PaymentGatewayException;
import roomescape.payment.domain.exception.PaymentAlreadyProcessedException;
import roomescape.payment.domain.exception.PaymentAmountMismatchException;
import roomescape.payment.domain.exception.PaymentInvalidRequestException;
import roomescape.payment.repository.PaymentOrderRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.ReservationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

        if (paymentOrderRepository.existsReadyOrder(name, date, timeId, themeId)) {
            throw new ConflictException("이미 결제 대기 중인 예약 요청이 있습니다.");
        }

        PaymentOrder paymentOrder = PaymentOrder.ready(
                generateOrderId(),
                paymentProperties.reservationAmount(),
                name,
                date,
                timeId,
                themeId,
                LocalDateTime.now()
        );

        return PaymentReadyOrder.from(paymentOrderRepository.save(paymentOrder));
    }

    @Transactional
    public Reservation confirm(String paymentKey, String orderId, long amount) {
        PaymentOrder paymentOrder = findOrder(orderId);

        if (paymentOrder.isConfirmed()) {
            throw new PaymentAlreadyProcessedException("이미 승인된 결제입니다.");
        }
        if (!paymentOrder.isReady()) {
            throw new PaymentInvalidRequestException("결제 대기 중인 주문이 아닙니다.");
        }
        if (!paymentOrder.hasAmount(amount)) {
            throw new PaymentAmountMismatchException("결제 금액이 주문 금액과 일치하지 않습니다.");
        }

        PaymentResult paymentResult = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));
        if (paymentResult.amount() != amount) {
            throw new PaymentGatewayException("결제 승인 응답 금액이 요청 금액과 일치하지 않습니다.");
        }

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

    private PaymentOrder findOrder(String orderId) {
        return paymentOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentInvalidRequestException("결제 주문을 찾을 수 없습니다."));
    }

    private String generateOrderId() {
        return ORDER_ID_PREFIX + UUID.randomUUID();
    }
}
