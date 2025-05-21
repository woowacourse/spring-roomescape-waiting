package roomescape.auth.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorCode;

@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    TOKEN_IS_EMPTY(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다"),
    COOKIE_IS_NULL(HttpStatus.UNAUTHORIZED, "쿠키가 존재하지 않습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "인증에 실패하였습니다"),
    PASSWORD_NOT_MATCHED(HttpStatus.UNAUTHORIZED, "비밀번호가 틀렸습니다."),
    ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
