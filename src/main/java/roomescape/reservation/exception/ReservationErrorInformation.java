package roomescape.reservation.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorInformation;

@Getter
@AllArgsConstructor
public enum ReservationErrorInformation implements ErrorInformation {

    RESERVATION_ID_IS_NULL(HttpStatus.BAD_REQUEST, "RES_001", "예약 ID가 누락되었습니다."),
    RESERVATION_MEMBER_IS_NULL(HttpStatus.BAD_REQUEST, "RES_002", "예약자가 누락되었습니다."),
    RESERVATION_DATE_IS_NULL(HttpStatus.BAD_REQUEST, "RES_003", "예약 날짜가 누락되었습니다."),
    RESERVATION_TIME_IS_NULL(HttpStatus.BAD_REQUEST, "RES_004", "예약 시간이 누락되었습니다."),
    RESERVATION_THEME_IS_NULL(HttpStatus.BAD_REQUEST, "RES_005", "예약 테마가 누락되었습니다."),

    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RES_006", "예약을 찾을 수 없습니다."),
    RESERVATION_ALREADY_BOOKED(HttpStatus.CONFLICT, "RES_007", "이미 예약된 일정입니다."),
    RESERVATION_ALREADY_CANCELED(HttpStatus.CONFLICT, "RES_008", "이미 취소된 예약입니다."),
    RESERVATION_NOT_OWNER(HttpStatus.FORBIDDEN, "RES_009", "본인의 예약만 취소할 수 있습니다."),
    RESERVATION_ALREADY_PAST(HttpStatus.CONFLICT, "RES_010", "이미 지난 예약입니다."),
    RESERVATION_PAST_DATETIME_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "RES_011",
        "과거 날짜/시간은 예약할 수 없습니다."),
    RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "RES_012",
        "과거 날짜/시간으로 예약 일정을 변경할 수 없습니다."),
    RESERVATION_ALREADY_WAITING(HttpStatus.BAD_REQUEST, "RES_013", "이미 대기중인 예약입니다."),
    RESERVATION_NOT_RESERVED(HttpStatus.BAD_REQUEST, "RES_014", "확정되지 않은 예약입니다.")
    ;

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;

}
