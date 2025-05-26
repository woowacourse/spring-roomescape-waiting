package roomescape.reservation.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import roomescape.global.response.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {

    PAST_RESERVATION("RSF001", "지난 날짜에는 예약할 수 없습니다."),
    NOT_SAME_SLOT("RSF002", "예약 슬롯을 생성할 수 없습니다."),
    NOT_EXISTS_WAITING("RSF003", "대기 중인 예약이 존재하지 않습니다.");

    private final String value;
    private final String message;
}
