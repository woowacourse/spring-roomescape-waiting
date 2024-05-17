package roomescape.exception;

public record ExceptionResponse(String errorCode, String message) {
    public static ExceptionResponse of(ErrorType errorType) {
        return new ExceptionResponse(errorType.getErrorCode(), errorType.getMessage());
    }
}
