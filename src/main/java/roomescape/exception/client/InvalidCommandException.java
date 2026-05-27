package roomescape.exception.client;

import org.springframework.http.HttpStatus;
import roomescape.exception.base.RoomeScapeClientException;

public class InvalidCommandException extends RoomeScapeClientException {
    public InvalidCommandException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
