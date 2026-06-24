package roomescape.payment.domain;

public class Payment {
    private final String orderId;
    private final Long amount;
    private final String paymentKey;
    private final Long reservationId;

    private Payment(String orderId, Long amount, String paymentKey, Long reservationId) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.reservationId = reservationId;
    }

    public static Payment restore(String orderId, Long amount, String paymentKey, Long reservationId) {
        return new Payment(orderId, amount, paymentKey, reservationId);
    }

    public static Payment pending(String orderId, Long amount, Long reservationId) {
        return new Payment(orderId, amount, null, reservationId);
    }

    public Payment confirm(String paymentKey) {
        return new Payment(orderId, amount, paymentKey, reservationId);
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public Long getReservationId() {
        return reservationId;
    }
}
