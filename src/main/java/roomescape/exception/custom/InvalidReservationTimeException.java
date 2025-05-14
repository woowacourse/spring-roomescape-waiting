package roomescape.exception.custom;

public class InvalidReservationTimeException extends IllegalArgumentException {

    public InvalidReservationTimeException(String message) {
        super(message);
    }
}
