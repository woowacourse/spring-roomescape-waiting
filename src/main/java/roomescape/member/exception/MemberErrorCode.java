package roomescape.member.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorCode;

@AllArgsConstructor
public enum MemberErrorCode implements ErrorCode {
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용중인 이메일입니다,"),
    NAME_REQUIRED(HttpStatus.BAD_REQUEST, "이름은 필수 입력값입니다."),
    INVALID_NAME(HttpStatus.BAD_REQUEST, "이름은 특수문자를 허용하지 않습니다."),
    PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "비밀번호는 필수 입력값입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "이메일은 필수 입력값입니다."),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "이메일의 형식이 올바르지 않습니다."),
    PASSWORD_NOT_MATCHED(HttpStatus.UNAUTHORIZED, "비밀번호가 틀렸습니다.");


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
