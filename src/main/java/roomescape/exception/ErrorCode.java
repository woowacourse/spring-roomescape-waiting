package roomescape.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 400 - BAD_REQUEST
    PAST_RESERVATION_CANCEL(HttpStatus.BAD_REQUEST, "이미 지난 예약은 취소할 수 없습니다."),
    PAST_RESERVATION_UPDATE(HttpStatus.BAD_REQUEST, "이미 지난 예약은 변경할 수 없습니다."),
    PAST_TIME_RESERVATION(HttpStatus.BAD_REQUEST, "이미 지난 시간으로 변경할 수 없습니다."),
    PAST_TIME_CREATE(HttpStatus.BAD_REQUEST, "이미 지난 시간에는 예약할 수 없습니다."),
    PAST_TIME_WAITING(HttpStatus.BAD_REQUEST, "이미 지난 시간에는 대기 신청할 수 없습니다."),
    PAST_WAITING_CANCEL(HttpStatus.BAD_REQUEST, "이미 지난 시간에는 대기를 취소할 수 없습니다."),

    // 400 - BAD_REQUEST (auth)
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 올바르지 않습니다."),

    // 401 - UNAUTHORIZED
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

    // 403 - FORBIDDEN
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 404 - NOT_FOUND
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 예약입니다."),
    TIME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 시간대입니다."),
    THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 테마입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),

    // 409 - CONFLICT
    DUPLICATE_RESERVATION(HttpStatus.CONFLICT, "이미 예약된 시간입니다."),
    DUPLICATE_WAITING(HttpStatus.CONFLICT, "같은 슬롯에 중복 대기할 수 없습니다."),
    TIME_HAS_RESERVATION(HttpStatus.CONFLICT, "예약이 존재하는 시간은 삭제할 수 없습니다."),
    THEME_HAS_RESERVATION(HttpStatus.CONFLICT, "예약이 존재하는 테마는 삭제할 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
