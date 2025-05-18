package roomescape.auth.web.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import roomescape.global.response.SuccessCode;

@RequiredArgsConstructor
@Getter
public enum AuthSuccessCode implements SuccessCode {

    LOGIN("ATS001", "로그인에 성공했습니다."),
    LOGOUT("ATS002", "로그아웃에 성공했습니다."),
    LOGIN_CHECK("ATS003", "로그인 상태를 확인했습니다.");

    private final String value;
    private final String message;
}
