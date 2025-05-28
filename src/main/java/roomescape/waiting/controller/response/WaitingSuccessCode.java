package roomescape.waiting.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import roomescape.global.response.SuccessCode;

@Getter
@RequiredArgsConstructor
public enum WaitingSuccessCode implements SuccessCode {

    WAIT("WTS001", "예약 대기 등록에 성공했습니다.");

    private final String value;
    private final String message;
}
