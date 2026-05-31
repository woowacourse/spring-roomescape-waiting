package roomescape.common.exception;

public record ErrorResponse(
        String code,
        String message
) {

    public static ErrorResponse of(final String code, final String message) {
        return new ErrorResponse(code, message);
    }
}
