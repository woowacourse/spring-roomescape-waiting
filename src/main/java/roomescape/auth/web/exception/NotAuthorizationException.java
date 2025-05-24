package roomescape.auth.web.exception;

public class NotAuthorizationException extends RuntimeException {
    public NotAuthorizationException(String message) {
        super(message);
    }
}
