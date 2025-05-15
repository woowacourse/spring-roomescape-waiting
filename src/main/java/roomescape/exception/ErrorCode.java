package roomescape.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "해당 정보를 찾을 수 없습니다"),
    DUPLICATED(HttpStatus.CONFLICT, "중복된 항목이 존재합니다"),
    DELETE_CONFLICT(HttpStatus.CONFLICT, "해당 항목에 대해 예약이 존재합니다"),
    NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED, "회원 정보를 찾을 수 없습니다");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
