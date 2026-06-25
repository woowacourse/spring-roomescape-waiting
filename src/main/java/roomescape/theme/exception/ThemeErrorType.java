package roomescape.theme.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorType;

public enum ThemeErrorType implements ErrorType {
    NOT_FOUND(HttpStatus.NOT_FOUND, "THEME404_001", "존재하지 않는 테마입니다."),
    IN_USE(HttpStatus.CONFLICT, "THEME409_001", "예약이 존재하는 테마는 삭제할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String errorMessage;

    ThemeErrorType(HttpStatus httpStatus, String errorCode, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
