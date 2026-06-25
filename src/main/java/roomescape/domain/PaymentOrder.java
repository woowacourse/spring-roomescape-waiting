package roomescape.domain;

import java.util.UUID;

public class PaymentOrder {
    private final Long id;
    private final String orderId;
    private final Long reservationId;
    private final Long amount;

    public static PaymentOrder create(Long reservationId, Long amount) {
        return new PaymentOrder(null, reservationId, generateOrderId(), amount);
    }

    public PaymentOrder(Long id, Long reservationId, String orderId, Long amount) {
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
