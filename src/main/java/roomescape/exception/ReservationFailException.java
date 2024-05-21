package roomescape.exception;

public class ReservationFailException extends IllegalArgumentException {
    public ReservationFailException(final String message) {
        super(message);
    }
}
