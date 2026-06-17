package roomescape.controller.dto;

import roomescape.domain.Order;

public class PaymentReadyResponse {

    private final String clientKey;
    private final String orderId;
    private final String orderName;
    private final Long amount;
    private final Long reservationId;

    private PaymentReadyResponse(String clientKey, String orderId, String orderName, Long amount, Long reservationId) {
        this.clientKey = clientKey;
        this.orderId = orderId;
        this.orderName = orderName;
        this.amount = amount;
        this.reservationId = reservationId;
    }

    public static PaymentReadyResponse from(String clientKey, Order order, String orderName) {
        return new PaymentReadyResponse(
                clientKey,
                order.getOrderId(),
                orderName,
                order.getAmount(),
                order.getReservationId()
        );
    }

    public String getClientKey() {
        return clientKey;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOrderName() {
        return orderName;
    }

    public Long getAmount() {
        return amount;
    }

    public Long getReservationId() {
        return reservationId;
    }
}
