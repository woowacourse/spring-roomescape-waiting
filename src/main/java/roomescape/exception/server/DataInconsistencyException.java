package roomescape.exception.server;

import org.springframework.http.HttpStatus;
import roomescape.exception.base.RoomeScapeServerException;

public class DataInconsistencyException extends RoomeScapeServerException {
    public DataInconsistencyException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
