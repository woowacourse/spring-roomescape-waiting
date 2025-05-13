package roomescape.exception;

public class InvalidReservationTimeException extends IllegalArgumentException {

    public InvalidReservationTimeException(String message) {
        super(message);
    }
}
