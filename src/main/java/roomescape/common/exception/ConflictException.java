package roomescape.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends CustomException {

    public ConflictException(String message) {
        super("CONFLICT", HttpStatus.CONFLICT, message);
    }
}
