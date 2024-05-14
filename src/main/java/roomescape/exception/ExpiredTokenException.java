package roomescape.exception;

public class ExpiredTokenException extends UnauthenticatedUserException {
    public static final int STATUS_CODE = UnauthenticatedUserException.STATUS_CODE;

    public ExpiredTokenException(final String message) {
        super(message);
    }
}
