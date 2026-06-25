package roomescape.domain;

import java.util.UUID;

public class PaymentOrder {
    public static final int MAX_IDEMPOTENCY_KEY_LENGTH = 300;

    private final Long id;
    private final String orderId;
    private final Long reservationId;
    private final Long amount;
    private final PaymentStatus status;

    public static PaymentOrder create(Long reservationId, Long amount) {
        return new PaymentOrder(null, reservationId, generateOrderId(), amount, PaymentStatus.PENDING);
    }

    public PaymentOrder(Long id, Long reservationId, String orderId, Long amount) {
        this(id, reservationId, orderId, amount, PaymentStatus.PENDING);
    }

    public PaymentOrder(Long id, Long reservationId, String orderId, Long amount, PaymentStatus status) {
        this.id = id;
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getIdempotencyKey() {
        return orderId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    private static String generateOrderId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
