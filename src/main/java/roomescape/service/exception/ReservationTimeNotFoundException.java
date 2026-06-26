package roomescape.service.exception;

public class ReservationTimeNotFoundException extends ResourceNotFoundException {
    public ReservationTimeNotFoundException(String message) {
        super(message);
    }
}
