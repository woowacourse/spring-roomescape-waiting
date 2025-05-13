package roomescape.exception;

public class InvalidReservationException extends IllegalArgumentException {

    public InvalidReservationException(String message) {
        super(message);
    }
}
