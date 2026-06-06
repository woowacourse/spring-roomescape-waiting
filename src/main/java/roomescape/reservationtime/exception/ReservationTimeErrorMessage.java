package roomescape.reservationtime.exception;

import roomescape.global.ErrorMessage;

public enum ReservationTimeErrorMessage implements ErrorMessage {

    TIME_NOT_FOUND("해당하는 ID(%s)의 시간이 존재하지 않습니다."),
    TIME_ALREADY_DELETED("이미 삭제된 시간입니다."),
    DUPLICATE_TIME("이미 존재하는 예약 시간입니다."),
    TIME_IN_USE("해당 시간에 예약이 존재하여 삭제할 수 없습니다."),
    ;

    private final String message;

    ReservationTimeErrorMessage(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
