package roomescape.payment.toss.dto;

public record TossErrorResponse(
        String code,
        String message
) {
}
