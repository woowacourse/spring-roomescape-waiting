package roomescape.domain.reservation;

public class PastReservationException extends IllegalArgumentException {

    public PastReservationException(final String message) {
        super(message);
    }
}
