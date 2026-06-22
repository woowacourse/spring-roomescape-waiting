package roomescape.payment;

public class Payment {
    private final Long id;
    private final Long reservationId;
    private final String paymentKey;
    private final String orderId;
    private final String idempotencyKey;
    private final PaymentStatus status;
    private final Long amount;

    public Payment(Long reservationId, String orderId, String idempotencyKey, Long amount) {
        this(null, reservationId, null, orderId, idempotencyKey, PaymentStatus.READY, amount);
    }

    public Payment(Long id, Long reservationId, String paymentKey, String orderId, String idempotencyKey,
                   PaymentStatus status, Long amount) {
        this.id = id;
        this.reservationId = reservationId;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Long getAmount() {
        return amount;
    }
}
