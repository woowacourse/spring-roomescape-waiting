package roomescape.support.exception.errors;

import lombok.Getter;

@Getter
public enum ReservationDateErrors implements Errors {

    RESERVATION_DATE_NOT_EXIST("존재하지 않는 날짜 입니다."),
    RESERVATION_DATE_IN_USE("이미 예약이 존재하는 날짜는 삭제할 수 없습니다."),
    RESERVATION_DATE_MUST_BE_TODAY_OR_LATER("예약 날짜는 오늘 이후여야 합니다. 오늘 날짜:%s"),
    PAST_RESERVATION_DATE_CANNOT_BE_DELETED("예전 예약은 삭제할 수 없습니다. 오늘 날짜:%s"),
    ;

    private final String message;

    ReservationDateErrors(String message) {
        this.message = message;
    }

    @Override
    public String getCode() {
        return name();
    }
}
