package roomescape.reservationtime.exception;

public class ReservationTimeInUseException extends RuntimeException {

    public ReservationTimeInUseException(final String message) {
        super(message);
    }
}
