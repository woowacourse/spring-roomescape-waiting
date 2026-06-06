package roomescape.reservation.application.exception;

public class ConflictException extends RuntimeException {

    public ConflictException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
    }
}
