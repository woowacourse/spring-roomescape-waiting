package roomescape.service.exception;

public class ReservationConflictException extends ResourceConflictException {
    public ReservationConflictException(String message) {
        super(message);
    }
}
