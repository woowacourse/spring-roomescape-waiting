package roomescape.reservation.application.exception;

public class BadRequestException extends RuntimeException {

    public BadRequestException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
    }

    public BadRequestException(String message) {
        super(message);
    }
}
