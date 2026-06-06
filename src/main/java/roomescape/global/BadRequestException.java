package roomescape.global;

public class BadRequestException extends RuntimeException {

    public BadRequestException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
    }

    public BadRequestException(ErrorMessage errorMessage, Object... args) {
        super(String.format(errorMessage.getMessage(), args));
    }
}
