package roomescape.payment.client.dto;

public record TossErrorResponse(
        String code,
        String message
) {
}
