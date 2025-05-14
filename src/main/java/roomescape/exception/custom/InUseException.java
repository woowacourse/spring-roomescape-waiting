package roomescape.exception.custom;

public class InUseException extends RuntimeException {

    public InUseException(String message) {
        super(message);
    }
}
