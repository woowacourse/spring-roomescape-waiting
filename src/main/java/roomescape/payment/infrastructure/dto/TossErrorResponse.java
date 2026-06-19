package roomescape.payment.infrastructure.dto;

public record TossErrorResponse(
        String code,
        String message
) {
}
