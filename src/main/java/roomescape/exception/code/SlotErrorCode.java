package roomescape.exception.code;

import org.springframework.http.HttpStatus;
import roomescape.exception.ErrorCode;

public enum SlotErrorCode implements ErrorCode {

    SLOT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 슬롯입니다."),
    SLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 슬롯입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    SlotErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
