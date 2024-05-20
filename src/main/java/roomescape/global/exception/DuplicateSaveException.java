package roomescape.global.exception;

public class DuplicateSaveException extends IllegalRequestException {

    public DuplicateSaveException(String message) {
        super(message);
    }

    public DuplicateSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
