package roomescape.member.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorInformation;

@Getter
@AllArgsConstructor
public enum MemberExceptionInformation implements ErrorInformation {

    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER_001", "해당 회원을 찾을 수 없습니다."),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "MEMBER_002", "비밀번호를 다시 입력해주세요."),
    NAME_IS_NULL(HttpStatus.BAD_REQUEST, "MEMBER_003", "회원 이름이 누락되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;

}
