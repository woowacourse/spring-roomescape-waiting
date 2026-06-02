package roomescape.exception;

public record ErrorResponse(
        String code,
        int status,
        String message
) {
}
