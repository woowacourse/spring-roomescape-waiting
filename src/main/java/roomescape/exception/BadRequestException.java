package roomescape.exception;

public class BadRequestException extends IllegalArgumentException {
    public BadRequestException() {
        super();
    }

    public BadRequestException(final String message) {
        super(message);
    }
}
