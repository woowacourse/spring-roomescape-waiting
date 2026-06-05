package roomescape.exception;

public class RoomescapeException extends RuntimeException {

    private final ErrorType errorType;

    public RoomescapeException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
