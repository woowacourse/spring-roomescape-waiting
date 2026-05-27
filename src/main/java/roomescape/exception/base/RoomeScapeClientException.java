package roomescape.exception.base;

import org.springframework.http.HttpStatus;

public abstract class RoomeScapeClientException extends RoomeScapeException {

    protected RoomeScapeClientException(String message, HttpStatus status) {
        super(message, status);
    }
}
