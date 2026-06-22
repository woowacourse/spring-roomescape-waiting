package roomescape.domain;

import lombok.Getter;

import java.util.UUID;

@Getter
public class Order {

    private final Long id;

    private final String orderId;
    private final String idempotencyKey;

    private final Long amount;
    private final String paymentKey;

    private final Long reservationId;
    private final OrderStatus status;

    private Order(Long id, String orderId, String idempotencyKey, Long amount, String paymentKey, Long reservationId, OrderStatus status) {
        this.id = id;
        this.orderId = orderId;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.reservationId = reservationId;
        this.status = status;
    }

    public static Order create(final Long amount, final Long reservationId) {
        String orderId = "order_" + UUID.randomUUID();
        String idempotencyKey = UUID.randomUUID().toString();
        return new Order(null, orderId, idempotencyKey, amount, null, reservationId, OrderStatus.PENDING);
    }

    public static Order createWithId(
            final Long id,
            final String orderId,
            final String idempotencyKey,
            final Long amount,
            final String paymentKey,
            final Long reservationId,
            final OrderStatus status
    ) {
        return new Order(id, orderId, idempotencyKey, amount, paymentKey, reservationId, status);
    }

    public Order withId(final Long id) {
        return new Order(id, this.orderId, this.idempotencyKey, this.amount, this.paymentKey, this.reservationId, this.status);
    }
}
