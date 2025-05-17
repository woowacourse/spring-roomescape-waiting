package roomescape.exception.custom;

public class InvalidReservationException extends IllegalArgumentException {

    public InvalidReservationException(String message) {
        super(message);
    }
}
