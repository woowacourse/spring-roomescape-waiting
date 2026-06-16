package roomescape.payment.domain;

public class Payment {

    private final Long id;
    private final String paymentKey;
    private final String orderId;
    private final long amount;
    private final String status;
    private final Long reservationId;

    private Payment(Long id, String paymentKey, String orderId, long amount, String status, Long reservationId) {
        this.id = id;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.reservationId = reservationId;
    }

    public static Payment of(String paymentKey, String orderId, long amount, String status, Long reservationId) {
        return new Payment(null, paymentKey, orderId, amount, status, reservationId);
    }

    public Long getId() { return id; }
    public String getPaymentKey() { return paymentKey; }
    public String getOrderId() { return orderId; }
    public long getAmount() { return amount; }
    public String getStatus() { return status; }
    public Long getReservationId() { return reservationId; }
}