package roomescape.support.exception.errors;

import lombok.Getter;

@Getter
public enum UserReservationErrors implements Errors {

    USER_RESERVATION_NOT_FOUND("사용자 예약 신청이 존재하지 않습니다."),
    ;

    private final String message;

    UserReservationErrors(String message) {
        this.message = message;
    }

    @Override
    public String getCode() {
        return name();
    }
}
