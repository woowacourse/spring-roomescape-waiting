package roomescape.exception;

import org.springframework.http.HttpStatus;

public class NoAdminPrivilegeException extends RuntimeException {
    public static final int STATUS_CODE = HttpStatus.FORBIDDEN.value();

    public NoAdminPrivilegeException(final String message) {
        super(message);
    }
}
