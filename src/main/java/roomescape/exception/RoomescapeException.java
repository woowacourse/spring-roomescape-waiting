package roomescape.exception;

public class RoomescapeException extends RuntimeException {

    private final ErrorType errorType;

    public RoomescapeException(ErrorType errorType, Object... args) {
        super(errorType.format(args));
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
