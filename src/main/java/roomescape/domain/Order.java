package roomescape.domain;

import roomescape.domain.exception.BusinessRuleViolationException;
import roomescape.domain.exception.ConflictException;

/**
 * 결제 전에 서버가 미리 저장해 두는 주문 정보. successUrl 의 amount 와 대조해 금액 위변조를 막는 기준값이다.
 */
public class Order {

    private static final String PAYMENT_KEY_REQUIRED = "결제 승인 키는 필수입니다.";
    private static final String ALREADY_CONFIRMED = "이미 확정된 주문입니다.";

    private final String orderId;
    private final String orderName;
    private final Long amount;
    private final Reservation reservation;
    private String paymentKey;
    private OrderStatus status;

    public Order(
            String orderId,
            String orderName,
            Long amount,
            Reservation reservation
    ) {
        this.orderId = orderId;
        this.orderName = orderName;
        this.amount = amount;
        this.reservation = reservation;
        this.status = OrderStatus.PENDING_PAYMENT;
    }

    public void confirm(String paymentKey) {
        validatePending();
        validatePaymentKey(paymentKey);

        this.paymentKey = paymentKey;
        this.status = OrderStatus.CONFIRMED;
    }

    private void validatePending() {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new ConflictException(ALREADY_CONFIRMED);
        }
    }

    private void validatePaymentKey(String paymentKey) {
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new BusinessRuleViolationException(PAYMENT_KEY_REQUIRED);
        }
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOrderName() {
        return orderName;
    }

    public Long getAmount() {
        return amount;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public OrderStatus getStatus() {
        return status;
    }

}
