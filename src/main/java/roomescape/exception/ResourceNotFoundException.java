package roomescape.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(final ErrorCode code, final String message) {
        super(code, HttpStatus.NOT_FOUND, message);
    }
}
