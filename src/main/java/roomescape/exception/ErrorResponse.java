package roomescape.exception;

public record ErrorResponse(
        String code,
        String detail
) {

    public static ErrorResponse from(final ErrorCode errorCode) {
        final String code = errorCode.name();
        final String detail = errorCode.getMessage();
        return new ErrorResponse(code, detail);
    }
}
