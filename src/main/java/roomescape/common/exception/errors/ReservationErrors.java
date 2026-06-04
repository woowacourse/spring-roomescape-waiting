package roomescape.common.exception.errors;

import lombok.Getter;

@Getter
public enum ReservationErrors implements Errors {

    USER_RESERVATION_NOT_FOUND("사용자 예약 신청이 존재하지 않습니다."),
    RESERVATION_NOT_FOUND("예약이 존재하지 않습니다."),
    ;

    private final String message;

    ReservationErrors(String message) {
        this.message = message;
    }

    @Override
    public String getCode() {
        return name();
    }
}
