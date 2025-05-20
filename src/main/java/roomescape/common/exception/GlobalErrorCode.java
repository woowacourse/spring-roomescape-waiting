package roomescape.common.exception;

import org.springframework.http.HttpStatus;

public enum GlobalErrorCode implements ErrorCode {
    ;

    @Override
    public HttpStatus getStatus() {
        return null;
    }

    @Override
    public String getMessage() {
        return "";
    }
}
