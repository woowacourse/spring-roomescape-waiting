package roomescape.payment.domain;

import roomescape.common.exception.RoomEscapeException;
import roomescape.payment.exception.PaymentErrorCode;

public class Order {

    private final Long id;
    private final Long reservationId;
    private final OrderId orderId;
    private final long amount;
    private final String paymentKey;
    private final PaymentStatus status;

    private Order(Long id, Long reservationId, OrderId orderId, long amount, String paymentKey,
                  PaymentStatus status) {
        validateReservationId(reservationId);
        validateOrderId(orderId);
        validateAmount(amount);
        validateStatus(status);
        this.id = id;
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.status = status;
    }

    public static Order create(Long reservationId, long amount) {
        return new Order(null, reservationId, OrderId.generate(), amount, null, PaymentStatus.PENDING);
    }

    public static Order of(Long id, Long reservationId, String orderId, long amount, String paymentKey,
                           PaymentStatus status) {
        return new Order(id, reservationId, new OrderId(orderId), amount, paymentKey, status);
    }

    private static void validateReservationId(Long reservationId) {
        if (reservationId == null) {
            throw new IllegalStateException("예약 식별자는 필수값입니다.");
        }
    }

    private static void validateOrderId(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalStateException("주문번호는 필수값입니다.");
        }
    }

    private static void validateAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalStateException("결제 금액은 0보다 커야 합니다. (입력값: " + amount + ")");
        }
    }

    private static void validateStatus(PaymentStatus status) {
        if (status == null) {
            throw new IllegalStateException("결제 상태는 필수값입니다.");
        }
    }

    public void verifyAmount(long requestedAmount) {
        if (amount != requestedAmount) {
            throw new RoomEscapeException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    public Order confirm(String paymentKey) {
        if (!status.isPending()) {
            throw new IllegalStateException("결제 대기 상태에서만 승인할 수 있습니다. (현재: " + status + ")");
        }
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new IllegalStateException("paymentKey는 필수값입니다.");
        }
        return new Order(id, reservationId, orderId, amount, paymentKey, PaymentStatus.DONE);
    }

    public Long getId() {
        return id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public long getAmount() {
        return amount;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public PaymentStatus getStatus() {
        return status;
    }
}
