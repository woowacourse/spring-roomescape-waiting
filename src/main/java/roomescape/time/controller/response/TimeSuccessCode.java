package roomescape.time.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import roomescape.global.response.SuccessCode;

@Getter
@RequiredArgsConstructor
public enum TimeSuccessCode implements SuccessCode {

    CREATE_TIME("TS001", "예약 시간이 등록되었습니다."),
    GET_TIMES("TS002", "모든 예약 시간을 조회했습니다."),
    DELETE_TIME("TS003", "예약 시간이 삭제되었습니다."),
    GET_AVAILABLE_TIMES("TS004", "예약 가능한 시간을 조회했습니다.");

    private final String value;
    private final String message;
}
