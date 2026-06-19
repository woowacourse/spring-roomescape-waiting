package roomescape.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.controller.dto.payment.PaymentOrderRequest;
import roomescape.domain.Member;
import roomescape.domain.Schedule;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentCheckoutProperties;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentOrder;
import roomescape.payment.PaymentOrderIdGenerator;
import roomescape.payment.PaymentResult;
import roomescape.repository.PaymentOrderDao;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentOrderDao paymentOrderDao;
    private final ScheduleService scheduleService;
    private final ReservationService reservationService;
    private final PaymentGateway paymentGateway;
    private final PaymentOrderIdGenerator orderIdGenerator;
    private final PaymentCheckoutProperties checkoutProperties;

    public PaymentService(
            PaymentOrderDao paymentOrderDao,
            ScheduleService scheduleService,
            ReservationService reservationService,
            PaymentGateway paymentGateway,
            PaymentOrderIdGenerator orderIdGenerator,
            PaymentCheckoutProperties checkoutProperties
    ) {
        this.paymentOrderDao = paymentOrderDao;
        this.scheduleService = scheduleService;
        this.reservationService = reservationService;
        this.paymentGateway = paymentGateway;
        this.orderIdGenerator = orderIdGenerator;
        this.checkoutProperties = checkoutProperties;
    }

    @Transactional
    public PaymentOrder createOrder(PaymentOrderRequest request, Member member) {
        validateCheckoutConfigured();
        LocalDateTime now = LocalDateTime.now();
        Schedule schedule = scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId());
        reservationService.validatePaymentOrderCreatable(member, schedule, now);

        int amount = schedule.getTheme().getPrice();
        PaymentOrder order = PaymentOrder.pending(
                orderIdGenerator.generate(),
                member.getId(),
                schedule.getId(),
                amount,
                now
        );

        paymentOrderDao.save(order);
        return order;
    }

    @Transactional
    public void confirm(String paymentKey, String orderId, int callbackAmount) {
        PaymentOrder order = getOrder(orderId);

        if (order.isConfirmedWith(paymentKey)) {
            return;
        }

        validateAmount(order, callbackAmount);
        reservationService.validateBeforeConfirm(order.getMemberId(), order.getScheduleId(), LocalDateTime.now());
        PaymentResult result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, order.getAmount()));

        Long reservationId = reservationService.saveConfirmedReservationByPayment(order.getScheduleId(), order.getMemberId());
        paymentOrderDao.confirm(orderId, result.paymentKey(), reservationId, LocalDateTime.now());
    }

    @Transactional
    public void fail(String code, String message, String orderId) {
        if (orderId != null && !orderId.isBlank()) {
            paymentOrderDao.fail(orderId, code, message, LocalDateTime.now());
        }
    }

    public String getClientKey() {
        return checkoutProperties.getClientKey();
    }

    private void validateCheckoutConfigured() {
        if (!checkoutProperties.isConfigured()) {
            throw new RoomescapeException(
                    DomainErrorCode.PAYMENT_AUTHENTICATION_FAILED,
                    "Toss 표준 결제창 클라이언트 키(test_ck_)가 설정되지 않았습니다."
            );
        }
    }

    private void validateAmount(PaymentOrder order, int callbackAmount) {
        if (order.getAmount() != callbackAmount) {
            throw new RoomescapeException(DomainErrorCode.PAYMENT_AMOUNT_MISMATCH, "결제 금액이 주문 금액과 일치하지 않습니다.");
        }
    }

    private PaymentOrder getOrder(String orderId) {
        return paymentOrderDao.findByOrderId(orderId)
                .orElseThrow(() -> new RoomescapeException(DomainErrorCode.NOT_FOUND_PAYMENT_ORDER, "주문 정보를 찾을 수 없습니다. orderId: " + orderId));
    }
}
