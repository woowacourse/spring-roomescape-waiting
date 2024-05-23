package roomescape.exception.reservation;

public abstract class ReservationException extends RuntimeException {

    protected ReservationException(String message) {
        super(message);
    }
}
