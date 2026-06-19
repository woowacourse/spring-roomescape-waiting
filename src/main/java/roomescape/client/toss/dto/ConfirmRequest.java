package roomescape.client.toss.dto;

public record ConfirmRequest(
        String paymentKey,
        String orderId,
        Long amount
) {
}
