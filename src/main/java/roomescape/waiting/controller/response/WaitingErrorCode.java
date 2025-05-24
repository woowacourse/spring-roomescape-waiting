package roomescape.waiting.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import roomescape.global.response.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum WaitingErrorCode implements ErrorCode {

    IN_ALREADY_WAITING("WF001", "이미 대기 중입니다."),
    ;

    private final String value;
    private final String message;
}
