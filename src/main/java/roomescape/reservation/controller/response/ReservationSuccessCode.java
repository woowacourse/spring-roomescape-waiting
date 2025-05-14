package roomescape.reservation.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import roomescape.global.response.SuccessCode;

@Getter
@RequiredArgsConstructor
public enum ReservationSuccessCode implements SuccessCode {

    RESERVE("RSS001", "예약에 성공했습니다."),
    GET_RESERVATIONS("RSS002", "예약을 모두 조회하였습니다."),
    CANCEL_RESERVATION("RSS003", "예약을 취소하였습니다."),
    SEARCH_RESERVATION("RSS004", "예약을 검색하였습니다.");

    private final String value;
    private final String message;
}
