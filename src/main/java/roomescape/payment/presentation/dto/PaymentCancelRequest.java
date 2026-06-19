package roomescape.payment.presentation.dto;

public record PaymentCancelRequest(
        String name,
        Long cancelAmount,
        String cancelReason
) {
}
