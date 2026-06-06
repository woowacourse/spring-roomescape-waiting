package roomescape.exception;

import org.springframework.http.HttpStatus;

public class InvalidInputException extends ApiException {

    public InvalidInputException(final ErrorCode code, final String message) {
        super(code, HttpStatus.BAD_REQUEST, message);
    }
}
