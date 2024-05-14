package roomescape.global.exception;

public class ViolationException extends RuntimeException {
    private final String message;

    public ViolationException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
