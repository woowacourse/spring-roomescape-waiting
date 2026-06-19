package roomescape.feature.payment.dto;

public record PaymentErrorResponse(
        String code,
        String message
) {
}
