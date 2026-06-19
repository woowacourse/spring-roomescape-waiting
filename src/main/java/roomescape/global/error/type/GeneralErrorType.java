package roomescape.global.error.type;

import org.springframework.http.HttpStatus;

public enum GeneralErrorType implements ErrorType {
    ILLEGAL_STATE(HttpStatus.BAD_REQUEST, "상태 값이 올바르지 않습니다.");

    private final HttpStatus status;
    private final String message;

    GeneralErrorType(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public String code() {
        return name();
    }
}
