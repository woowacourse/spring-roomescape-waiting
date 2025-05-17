package roomescape.reservation.domain.exception;

public class PastReservationException extends RuntimeException {
    public PastReservationException(String message) {
        super(message);
    }
}
