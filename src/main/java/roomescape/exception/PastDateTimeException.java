package roomescape.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PastDateTimeException extends RuntimeException {

    public PastDateTimeException(String message) {
        super(message);
    }
}
