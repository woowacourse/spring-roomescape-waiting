package roomescape.error;

import org.springframework.http.HttpStatus;

public class DataInconsistencyException extends BusinessException {
    public DataInconsistencyException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
}
