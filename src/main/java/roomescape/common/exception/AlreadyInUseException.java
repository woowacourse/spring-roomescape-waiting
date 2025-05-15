package roomescape.common.exception;

public class AlreadyInUseException extends RuntimeException {

    public AlreadyInUseException(final String message) {
        super(message);
    }
}
