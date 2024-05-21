package roomescape.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHROZIED(HttpStatus.UNAUTHORIZED, "인증되지 않았습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류입니다."),
    ;

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
