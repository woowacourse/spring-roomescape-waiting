package roomescape.support.exception.errors;

import lombok.Getter;

@Getter
public enum ReservationTimeErrors implements Errors {

    INVALID_RESERVATION_TIME("시간은 필수입니다."),
    INVALID_RESERVATION_TIME_FORMAT("시간은 HH:MM 형식이어야 합니다."),
    RESERVATION_TIME_NOT_EXIST("존재하지 않는 예약 시간대 입니다."),
    RESERVATION_TIME_IN_USE("이미 예약이 존재하는 시간대는 삭제할 수 없습니다."),
    RESERVATION_TIME_SHOULD_BE_NOW_OR_LATER("예약 시간은 현재 이후여야 합니다. 현재 시각:%s"),
    PAST_RESERVATION_TiME_CANNOT_BE_DELETED("현재보다 이전 시간 예약을 삭제할 수 없습니다. 현재 시각:%s"),
    ;

    private final String message;

    ReservationTimeErrors(String message) {
        this.message = message;
    }

    @Override
    public String getCode() {
        return name();
    }
}
