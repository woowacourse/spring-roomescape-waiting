package roomescape.member.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import roomescape.global.response.SuccessCode;

@RequiredArgsConstructor
@Getter
public enum MemberSuccessCode implements SuccessCode {

    SIGN_UP("MBS001", "회원가입에 성공했습니다."),
    GET_MEMBERS("MBS002", "모든 회원을 조회하였습니다.");

    private final String value;
    private final String message;
}
