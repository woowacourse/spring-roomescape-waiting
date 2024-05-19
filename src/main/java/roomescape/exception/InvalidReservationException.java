package roomescape.exception;

public class InvalidReservationException extends RuntimeException {
    public InvalidReservationException() {
        super();
    }

    public InvalidReservationException(final String message) {
        super(message);
    }

    public InvalidReservationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InvalidReservationException(final Throwable cause) {
        super(cause);
    }
}
