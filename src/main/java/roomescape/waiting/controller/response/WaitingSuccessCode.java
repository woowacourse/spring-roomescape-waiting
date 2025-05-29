package roomescape.waiting.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import roomescape.global.response.SuccessCode;

@Getter
@RequiredArgsConstructor
public enum WaitingSuccessCode implements SuccessCode {

    WAIT("WTS001", "예약 대기 등록에 성공했습니다."),
    GET_WAITINGS("WTS002", "예약 대기를 모두 조회하였습니다."),
    CANCEL_WAITING_BY_USER("RSS006", "사용자가 예약을 취소하였습니다.");

    private final String value;
    private final String message;
}
