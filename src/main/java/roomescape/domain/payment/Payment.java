package roomescape.domain.payment;

public class Payment {

    private final Long id;
    private final Long reservationId;
    private final String paymentKey;
    private final String orderId;
    private final Long amount;
    private final PaymentStatus status;

    public Payment(Long reservationId, String paymentKey, String orderId, Long amount) {
        this(null, reservationId, paymentKey, orderId, amount, PaymentStatus.CONFIRMED);
    }

    public Payment(Long reservationId, String paymentKey, String orderId, Long amount, PaymentStatus status) {
        this(null, reservationId, paymentKey, orderId, amount, status);
    }

    public Payment(Long id, Long reservationId, String paymentKey, String orderId, Long amount) {
        this(id, reservationId, paymentKey, orderId, amount, PaymentStatus.CONFIRMED);
    }

    public Payment(Long id, Long reservationId, String paymentKey, String orderId, Long amount, PaymentStatus status) {
        this.id = id;
        this.reservationId = reservationId;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
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

    public PaymentStatus getStatus() {
        return status;
    }
}
