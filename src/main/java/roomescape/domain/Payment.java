package roomescape.domain;

public class Payment {

    private final Long id;
    private final String orderId;
    private final long amount;
    private final String paymentKey;
    private final Long reservationId;

    public Payment(
            Long id,
            String orderId,
            long amount,
            String paymentKey,
            Long reservationId
    ) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.reservationId = reservationId;
    }

    public Payment(String orderId, long amount, Long reservationId) {
        this(null, orderId, amount, null, reservationId);
    }

    public Payment withPaymentKey(String paymentKey) {
        return new Payment(id, orderId, amount, paymentKey, reservationId);
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public long getAmount() {
        return amount;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public Long getReservationId() {
        return reservationId;
    }
}
