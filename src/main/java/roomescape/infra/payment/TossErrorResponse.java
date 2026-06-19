package roomescape.infra.payment;

public record TossErrorResponse(
        String code,
        String message
) {
}
