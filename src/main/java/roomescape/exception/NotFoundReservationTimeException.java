package roomescape.exception;

public class NotFoundReservationTimeException extends RuntimeException {
    public NotFoundReservationTimeException(String message) {
        super(message);
    }
}
