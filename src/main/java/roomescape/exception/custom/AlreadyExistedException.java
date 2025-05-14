package roomescape.exception.custom;

public class AlreadyExistedException extends RuntimeException {

    public AlreadyExistedException(String message) {
        super(message);
    }
}
