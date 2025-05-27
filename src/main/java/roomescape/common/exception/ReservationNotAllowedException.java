package roomescape.common.exception;

public class ReservationNotAllowedException extends RuntimeException {
    public ReservationNotAllowedException(String message) {
        super(message);
    }
}
