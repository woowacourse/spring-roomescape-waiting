package roomescape.reservation.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.model.ExceptionCode;

public enum ReservationExceptionCode implements ExceptionCode {

    RESERVATION_TIME_IS_PAST_EXCEPTION(HttpStatus.BAD_REQUEST, "지난 시간의 테마를 선택했습니다."),
    RESERVATION_DATE_IS_PAST_EXCEPTION(HttpStatus.BAD_REQUEST, "지난 날짜의 예약을 시도하였습니다."),
    RESERVATION_DATE_IS_OVER_RANGE_EXCEPTION(HttpStatus.BAD_REQUEST, "가능한 날짜의 예약이 아닙니다."),
    DUPLICATE_RESERVATION(HttpStatus.BAD_REQUEST, "중복되는 예약입니다."),
    CAN_NOT_CANCEL_AFTER_MIN_CANCEL_DATE(HttpStatus.BAD_REQUEST, "하루 전 예약 취소는 불가합니다."),
    WAITING_IS_MAX(HttpStatus.BAD_REQUEST, "대기 인원이 가득 찼습니다."),
    THEME_INFO_IS_NULL_EXCEPTION(HttpStatus.BAD_REQUEST, "필터링할 테마 정보가 존재하지 않습니다."),
    MEMBER_INFO_IS_NULL_EXCEPTION(HttpStatus.BAD_REQUEST, "필터링할 유저 정보가 존재하지 않습니다."),
    DATE_IS_NULL_EXCEPTION(HttpStatus.BAD_REQUEST, "필터링할 날짜 정보가 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ReservationExceptionCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
