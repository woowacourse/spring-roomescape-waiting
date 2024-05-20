package roomescape.member.exception;

import org.springframework.http.HttpStatus;
import roomescape.exception.model.ExceptionCode;

public enum MemberExceptionCode implements ExceptionCode {

    ILLEGAL_EMAIL_FORM_EXCEPTION(HttpStatus.BAD_REQUEST, "이메일 형식이 맞지 않습니다."),
    ILLEGAL_PASSWORD_FORM_EXCEPTION(HttpStatus.BAD_REQUEST, "비밀번호는 숫자와 소문자로 이루어져야 합니다."),
    MEMBER_NOT_EXIST_EXCEPTION(HttpStatus.BAD_REQUEST, "해당하는 유저가 존재하지 않습니다."),
    MEMBER_ROLE_NOT_EXIST_EXCEPTION(HttpStatus.BAD_REQUEST, "해당하는 권한의 유저가 존재하지 않습니다."),

    MEMBER_ROLE_UN_AUTHORIZED_EXCEPTION(HttpStatus.FORBIDDEN, "접근 가능한 권한이 아닙니다."),

    ID_AND_PASSWORD_NOT_MATCH_OR_EXIST(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않거나 회원가입하지 않은 유저입니다.");

    private final HttpStatus httpStatus;
    private final String message;

    MemberExceptionCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
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
