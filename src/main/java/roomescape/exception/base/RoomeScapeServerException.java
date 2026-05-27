package roomescape.exception.base;

import org.springframework.http.HttpStatus;

public abstract class RoomeScapeServerException extends RoomeScapeException {
    protected RoomeScapeServerException(String message, HttpStatus status) {
        super(message, status);
    }
}
