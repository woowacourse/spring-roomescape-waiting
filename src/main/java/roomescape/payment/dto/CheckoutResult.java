package roomescape.payment.dto;

public record CheckoutResult(String orderId, String orderName, long amount) {}
