package roomescape.domain;

public record PaymentResult(
        String orderId,
        String status,
        Long approvedAmount,
        String approvedAt
) {
}
