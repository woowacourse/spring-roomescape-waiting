package roomescape.reservation.infra.toss.dto;

public record ConfirmRequest(
        String paymentKey,
        String orderId,
        Long amount
) {
}
