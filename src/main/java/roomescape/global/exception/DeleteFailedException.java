package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public class DeleteFailedException extends BusinessException{

    public DeleteFailedException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
