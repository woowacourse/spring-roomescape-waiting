package roomescape.service.exception;

public class ReservationTimeInUseException extends ResourceConflictException {
    public ReservationTimeInUseException(String message) {
        super(message);
    }
}
