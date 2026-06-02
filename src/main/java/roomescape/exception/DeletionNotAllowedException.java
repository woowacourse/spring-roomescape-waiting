package roomescape.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DeletionNotAllowedException extends RuntimeException {
    public DeletionNotAllowedException(String message) {
        super(message);
    }
}
