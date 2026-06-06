package roomescape.reservation.exception;

import roomescape.global.ErrorMessage;

public enum ReservationErrorMessage implements ErrorMessage {

    RESERVATION_NOT_FOUND("해당하는 ID(%s)의 예약이 존재하지 않습니다."),
    DUPLICATE_RESERVATION("이미 중복된 예약이 존재합니다."),
    CANNOT_SELECT_PAST_DATETIME("지나간 날짜, 시간에 대한 예약 요청은 불가능합니다."),

    WAITING_NOT_FOUND("해당하는 ID(%s)의 대기가 존재하지 않습니다."),
    DUPLICATE_WAITING("이미 중복된 대기가 존재합니다."),
    ALREADY_RESERVED_CANNOT_WAIT("이미 예약이 존재하는 슬롯에는 대기 신청이 불가합니다."),

    FORBIDDEN_RESERVATION_ACCESS("본인의 예약만 변경하거나 취소할 수 있습니다."),
    FORBIDDEN_WAITING_ACCESS("본인의 대기만 취소할 수 있습니다."),
    CANNOT_SELECT_PAST_RESERVATION_TIME("현재 시간보다 이전 시간으로 예약할 수 없습니다."),
    CANNOT_MODIFY_PAST_RESERVATION("지난 예약은 변경하거나 취소할 수 없습니다."),
    ;

    private final String message;

    ReservationErrorMessage(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
