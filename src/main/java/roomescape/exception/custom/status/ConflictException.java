package roomescape.exception.custom.status;

import org.springframework.http.HttpStatus;

public class ConflictException extends CustomException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
