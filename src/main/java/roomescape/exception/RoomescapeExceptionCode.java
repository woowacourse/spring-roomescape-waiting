package roomescape.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum RoomescapeExceptionCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 예약입니다."),
    TIME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 예약 시간입니다."),
    WAITING_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 예약 대기입니다."),
    THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 테마입니다."),
    WAITING_FOR_NO_RESERVATION(HttpStatus.BAD_REQUEST, "예약이 없는 건에는 예약 대기를 할 수 없습니다."),
    WAITING_FOR_MY_RESERVATION(HttpStatus.BAD_REQUEST, "이미 예약한 건에는 예약 대기를 할 수 없습니다."),
    WAITING_DUPLICATED(HttpStatus.BAD_REQUEST, "중복된 예약 대기를 할 수 없습니다."),
    INVALID_DATETIME(HttpStatus.BAD_REQUEST, "지나간 시간에 대한 예약은 할 수 없습니다."),
    RESERVATION_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "예약이 이미 존재합니다."),
    EMPTY_NAME(HttpStatus.BAD_REQUEST, "예약자 이름은 비어있을 수 없습니다."),
    EMPTY_TIME(HttpStatus.BAD_REQUEST, "예약 시간이 비어 있습니다."),
    EMPTY_DATE(HttpStatus.BAD_REQUEST, "예약 날짜가 비어있습니다."),
    INVALID_NAME_FORMAT(HttpStatus.BAD_REQUEST, "예약자 이름은 숫자로만 구성될 수 없습니다."),
    INVALID_TIME_FORMAT(HttpStatus.BAD_REQUEST, "유효하지 않은 예약 시간입니다."),
    INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "유효하지 않은 예약 날짜입니다."),
    CANNOT_WAIT_FOR_MY_RESERVATION(HttpStatus.BAD_REQUEST, "내 예약에 대기할 수 없습니다."),
    CANNOT_DELETE_TIME_REFERENCED_BY_RESERVATION(HttpStatus.BAD_REQUEST, "해당 시간에 예약이 존재하기 때문에 삭제할 수 없습니다."),
    CANNOT_DELETE_THEME_REFERENCED_BY_RESERVATION(HttpStatus.BAD_REQUEST, "해당 테마에 예약이 존재하기 때문에 삭제할 수 없습니다."),
    ;

    private final HttpStatusCode httpStatusCode;
    private final String message;

    RoomescapeExceptionCode(HttpStatusCode httpStatusCode, String message) {
        this.httpStatusCode = httpStatusCode;
        this.message = message;
    }

    public HttpStatusCode httpStatusCode() {
        return httpStatusCode;
    }

    public String message() {
        return message;
    }
}
