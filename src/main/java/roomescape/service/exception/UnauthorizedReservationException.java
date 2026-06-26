package roomescape.service.exception;

public class UnauthorizedReservationException extends RuntimeException {
    public UnauthorizedReservationException(String message) {
        super(message);
    }
}
