package roomescape.exception;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    protected ApiException(final ErrorCode code, final HttpStatus status, final String message) {
        this(code.getCode(), status, message);
    }

    protected ApiException(final String code, final HttpStatus status, final String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
