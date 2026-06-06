package roomescape.reservation.application.exception;

import lombok.Getter;

@Getter
public enum ErrorMessage {

    RESERVATION_NOT_FOUND("해당하는 ID(%s)의 예약이 존재하지 않습니다."),
    DUPLICATE_RESERVATION("이미 중복된 예약이 존재합니다."),
    CANNOT_SELECT_PAST_DATETIME("지나간 날짜, 시간에 대한 예약 요청은 불가능합니다."),

    WAITING_NOT_FOUND("해당하는 ID(%s)의 대기가 존재하지 않습니다."),
    DUPLICATE_WAITING("이미 중복된 대기가 존재합니다."),

    TIME_NOT_FOUND("해당하는 ID(%s)의 시간이 존재하지 않습니다."),
    TIME_ALREADY_DELETED("이미 삭제된 시간입니다."),
    DUPLICATE_TIME("이미 존재하는 예약 시간입니다."),
    TIME_IN_USE("해당 시간에 예약이 존재하여 삭제할 수 없습니다."),

    THEME_NOT_FOUND("해당하는 ID(%s)의 테마가 존재하지 않습니다."),
    THEME_ALREADY_DELETED("이미 삭제된 테마입니다."),
    DUPLICATE_THEME("이미 존재하는 테마 이름입니다."),
    THEME_IN_USE("해당 테마에 예약이 존재하여 삭제할 수 없습니다."),

    INVALID_DATA_FORMAT("요청 데이터의 형식이 올바르지 않습니다."),
    ;

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String format(Object... args) {
        return String.format(message, args);
    }
}
