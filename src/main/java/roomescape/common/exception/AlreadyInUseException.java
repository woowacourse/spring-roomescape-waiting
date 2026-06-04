package roomescape.common.exception;

import org.springframework.http.HttpStatus;

public class AlreadyInUseException extends CustomException {

    public AlreadyInUseException(String message) {
        super("ALREADY_IN_USE", HttpStatus.CONFLICT, message);
    }
}
