package roomescape.global;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
    }
}
