package roomescape.exception;

import org.springframework.http.HttpStatus;

public enum WaitingErrorCode implements ErrorCode {
    INVALID_WAITING_NUMBER(HttpStatus.BAD_REQUEST, "대기 순서는 필수입니다.");

    private final HttpStatus httpStatus;
    private final String message;

    WaitingErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getErrorMessage() {
        return message;
    }

    @Override
    public String getErrorName() {
        return this.name();
    }
}
