package roomescape.payment.dto;

public record PaymentResult(String paymentKey, String orderId, String status, long totalAmount) {}
