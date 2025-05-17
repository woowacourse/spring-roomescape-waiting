package roomescape.exception.reservation;

public class InvalidReservationException extends IllegalArgumentException {

    public InvalidReservationException(String message) {
        super(message);
    }
}
