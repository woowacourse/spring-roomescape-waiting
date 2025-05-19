package roomescape.global.auth.exception;

public class UnAuthorizedException extends RuntimeException {

    public UnAuthorizedException(final String message) {
        super(message);
    }
}
