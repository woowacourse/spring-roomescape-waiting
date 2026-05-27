package roomescape.exception.client;

import org.springframework.http.HttpStatus;
import roomescape.exception.base.RoomeScapeClientException;

public class BusinessRuleViolationException extends RoomeScapeClientException {
    public BusinessRuleViolationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
