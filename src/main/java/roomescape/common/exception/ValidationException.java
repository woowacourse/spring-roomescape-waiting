package roomescape.common.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends CustomException {

    public ValidationException(String message) {
        super("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, message);
    }
}
