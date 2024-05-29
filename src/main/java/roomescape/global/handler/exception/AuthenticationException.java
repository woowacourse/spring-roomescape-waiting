package roomescape.global.handler.exception;

public class AuthenticationException extends RuntimeException {

    private final String message;

    public AuthenticationException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
