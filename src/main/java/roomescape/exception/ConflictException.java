package roomescape.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {

    public ConflictException(final ErrorCode code, final String message) {
        super(code, HttpStatus.CONFLICT, message);
    }
}
