package roomescape.auth.web.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import roomescape.global.response.ErrorCode;

@RequiredArgsConstructor
@Getter
public enum AuthErrorCode implements ErrorCode {

    NOT_ADMIN("ATF001", "권한이 없습니다."),
    NOT_AUTHORIZED("ATF002", "인증 실패");

    private final String value;
    private final String message;
}
