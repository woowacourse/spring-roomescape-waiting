package roomescape.global;

public class NotFoundException extends RuntimeException {

    public NotFoundException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
    }

    public NotFoundException(ErrorMessage errorMessage, Object... args) {
        super(String.format(errorMessage.getMessage(), args));
    }
}
