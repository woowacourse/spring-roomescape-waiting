package roomescape.common.exception;

import org.springframework.http.HttpStatus;

public class InactiveException extends CustomException {

    public InactiveException(String message) {
        super("INACTIVE", HttpStatus.BAD_REQUEST, message);
    }
}
