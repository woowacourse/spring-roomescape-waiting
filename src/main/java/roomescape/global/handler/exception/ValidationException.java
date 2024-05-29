package roomescape.global.handler.exception;

public class ValidationException extends RuntimeException {

    private final String message;

    public ValidationException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
