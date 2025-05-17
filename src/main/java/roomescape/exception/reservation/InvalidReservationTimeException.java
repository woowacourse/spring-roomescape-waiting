package roomescape.exception.reservation;

public class InvalidReservationTimeException extends IllegalArgumentException {

    public InvalidReservationTimeException(String message) {
        super(message);
    }
}
