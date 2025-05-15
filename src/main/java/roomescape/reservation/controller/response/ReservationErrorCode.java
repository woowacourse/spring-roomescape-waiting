package roomescape.reservation.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import roomescape.global.response.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {

    PAST_RESERVATION("RSF001", "지난 날짜에는 예약할 수 없습니다.");

    private final String value;
    private final String message;
}
