package roomescape.domain;

public class Payment {

    private final Long id;
    private final String orderId;
    private final long amount;
    private final String paymentKey;
    private final Long reservationId;
    private final OrderStatus status;

    public Payment(
            Long id,
            String orderId,
            long amount,
            String paymentKey,
            Long reservationId,
            OrderStatus status
    ) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.reservationId = reservationId;
        this.status = status;
    }

    public Payment(Long id, String orderId, long amount, String paymentKey, Long reservationId) {
        this(id, orderId, amount, paymentKey, reservationId, OrderStatus.PENDING);
    }

    public Payment(String orderId, long amount, Long reservationId) {
        this(null, orderId, amount, null, reservationId, OrderStatus.PENDING);
    }

    public Payment withPaymentKey(String paymentKey) {
        return new Payment(id, orderId, amount, paymentKey, reservationId, status);
    }

    public OrderStatus getStatus() {
        return status;
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
