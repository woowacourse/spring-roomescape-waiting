package roomescape.service.exception;

public class PastReservationException extends RuntimeException {
    public PastReservationException(String message) {
        super(message);
    }
}
