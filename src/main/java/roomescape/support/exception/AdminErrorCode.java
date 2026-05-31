package roomescape.support.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AdminErrorCode implements ErrorCode {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,
        "인증 정보가 누락되었거나 유효하지 않습니다.", "X-ADMIN-TOKEN 헤더의 유효성 및 관리자 권한 여부를 확인하십시오."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String action;

    AdminErrorCode(HttpStatus httpStatus, String message, String action) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.action = action;
    }

    @Override
    public String getCode() {
        return name();
    }
}
