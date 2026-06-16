package roomescape.controller.dto;

import roomescape.domain.Order;

public class PaymentReadyResponse {

    private final String clientKey;
    private final String orderId;
    private final String orderName;
    private final Long amount;

    private PaymentReadyResponse(String clientKey, String orderId, String orderName, Long amount) {
        this.clientKey = clientKey;
        this.orderId = orderId;
        this.orderName = orderName;
        this.amount = amount;
    }

    public static PaymentReadyResponse from(String clientKey, Order order, String orderName) {
        return new PaymentReadyResponse(clientKey, order.getOrderId(), orderName, order.getAmount());
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
}
