package roomescape.auth.web.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import roomescape.global.response.ErrorCode;

@RequiredArgsConstructor
@Getter
public enum AuthErrorCode implements ErrorCode {

    NOT_ADMIN("ATF001"),
    NOT_AUTHORIZED("ATF002");

    private final String value;
}
