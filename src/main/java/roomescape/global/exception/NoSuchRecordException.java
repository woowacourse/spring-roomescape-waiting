package roomescape.global.exception;

public class NoSuchRecordException extends IllegalRequestException {

    public NoSuchRecordException(String message) {
        super(message);
    }

    public NoSuchRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}
