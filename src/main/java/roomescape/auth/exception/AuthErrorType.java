package roomescape.auth.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorType;

public enum AuthErrorType implements ErrorType {
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH401_001", "이메일 또는 비밀번호가 일치하지 않습니다."),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH401_002", "로그인이 필요한 요청입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401_003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401_004", "토큰이 만료되었습니다. 다시 로그인해 주세요."),
    INSUFFICIENT_ROLE(HttpStatus.FORBIDDEN, "AUTH403_001", "권한이 없습니다."),
    WRONG_STORE_ACCESS(HttpStatus.FORBIDDEN, "AUTH403_002", "다른 매장에 접근권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String errorMessage;

    AuthErrorType(HttpStatus httpStatus, String errorCode, String errorMessage) {
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
