package roomescape.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    PAST_RESERVATION(HttpStatus.BAD_REQUEST, "이미 지난 시간으로는 예약할 수 없습니다."),
    PAST_RESERVATION_LOCKED(HttpStatus.CONFLICT, "이미 지난 예약은 변경하거나 취소할 수 없습니다."),
    FORBIDDEN_RESERVATION(HttpStatus.FORBIDDEN, "본인의 예약만 변경하거나 취소할 수 있습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 리소스입니다."),
    DUPLICATE_RESERVATION(HttpStatus.CONFLICT, "이미 예약된 시간입니다."),
    RESERVATION_OPERATION_CONFLICT(HttpStatus.CONFLICT,
            "일시적인 문제로 예약 작업을 완료하지 못했습니다. 잠시 후 다시 시도해 주세요."),
    WAITING_NOT_ALLOWED_FOR_OWN_RESERVATION(HttpStatus.CONFLICT, "본인이 예약한 시간에는 대기를 신청할 수 없습니다."),
    UNCHANGED_RESERVATION(HttpStatus.CONFLICT, "기존 예약과 같은 날짜·시간으로는 변경할 수 없습니다."),
    RESOURCE_IN_USE(HttpStatus.CONFLICT, "예약이 존재하는 리소스는 삭제할 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다.");

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
