package roomescape.exception.client;

import org.springframework.http.HttpStatus;
import roomescape.exception.base.RoomeScapeClientException;

public class ResourceNotFoundException extends RoomeScapeClientException {
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
