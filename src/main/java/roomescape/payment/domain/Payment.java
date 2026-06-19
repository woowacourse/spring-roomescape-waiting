package roomescape.payment.domain;

public class Payment {

    private final Long id;
    private final String paymentKey;
    private final String orderId;
    private final long amount;
    private final String status;
    private final int cancelAttempts;
    private final Long reservationId;

    private Payment(Long id, String paymentKey, String orderId, long amount, String status, int cancelAttempts, Long reservationId) {
        this.id = id;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.cancelAttempts = cancelAttempts;
        this.reservationId = reservationId;
    }

    public static Payment of(String paymentKey, String orderId, long amount, String status, Long reservationId) {
        return new Payment(null, paymentKey, orderId, amount, status, 0, reservationId);
    }

    static Payment fromRow(String paymentKey, String orderId, long amount, String status, int cancelAttempts, Long reservationId) {
        return new Payment(null, paymentKey, orderId, amount, status, cancelAttempts, reservationId);
    }

    public Long getId() { return id; }
    public String getPaymentKey() { return paymentKey; }
    public String getOrderId() { return orderId; }
    public long getAmount() { return amount; }
    public String getStatus() { return status; }
    public int getCancelAttempts() { return cancelAttempts; }
    public Long getReservationId() { return reservationId; }
}