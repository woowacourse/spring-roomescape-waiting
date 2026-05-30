package roomescape.global.exception;

public class UniqueConstraintViolationException extends RuntimeException {

    public UniqueConstraintViolationException(Throwable cause) {
        super(cause);
    }
}
