package roomescape.exception;

import org.springframework.http.HttpStatus;

public class UnauthenticatedUserException extends RuntimeException {
    public static final int STATUS_CODE = HttpStatus.UNAUTHORIZED.value();

    public UnauthenticatedUserException(final String message) {
        super(message);
    }
}
