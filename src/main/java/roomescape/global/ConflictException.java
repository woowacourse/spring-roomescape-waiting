package roomescape.global;

public class ConflictException extends RuntimeException {

    public ConflictException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
    }
}
