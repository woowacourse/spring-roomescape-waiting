package roomescape.domain.payment;

public class Payment {

    private final Long id;
    private final Long reservationId;
    private final String paymentKey;
    private final String orderId;
    private final Long amount;

    public Payment(Long reservationId, String paymentKey, String orderId, Long amount) {
        this.id = null;
        this.reservationId = reservationId;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
    }

    public Payment(Long id, Long reservationId, String paymentKey, String orderId, Long amount) {
        this.id = id;
        this.reservationId = reservationId;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
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

    public Long getAmount() {
        return amount;
    }
}
