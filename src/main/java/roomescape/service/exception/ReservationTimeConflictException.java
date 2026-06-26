package roomescape.service.exception;

public class ReservationTimeConflictException extends ResourceConflictException {
    public ReservationTimeConflictException(String message) {
        super(message);
    }
}
