package roomescape.client.dto;

public record ConfirmRequest(
        String paymentKey,
        String orderId,
        Long amount
) {
}
