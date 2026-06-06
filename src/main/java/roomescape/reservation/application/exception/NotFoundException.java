package roomescape.reservation.application.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
    }

    public NotFoundException(String message) {
        super(message);
    }
}
