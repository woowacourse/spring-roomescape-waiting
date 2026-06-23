package roomescape.domain;

import java.util.UUID;

public class Order {
    private final Long id;
    private final String orderId;
    private final Long reservationId;
    private final Long amount;

    public static Order create(Long reservationId, Long amount) {
        return new Order(null, reservationId, generateOrderId(), amount);
    }

    public Order(Long id, Long reservationId, String orderId, Long amount) {
        this.id = id;
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getAmount() {
        return amount;
    }

    private static String generateOrderId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
