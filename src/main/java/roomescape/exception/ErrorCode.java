package roomescape.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    PAST_SCHEDULE(HttpStatus.BAD_REQUEST, "이미 지난 시간입니다."),
    PAST_RESOURCE_LOCKED(HttpStatus.CONFLICT, "이미 지난 리소스는 변경하거나 취소할 수 없습니다."),
    FORBIDDEN_RESOURCE(HttpStatus.FORBIDDEN, "접근 권한이 없는 리소스입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 리소스입니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "이미 존재하는 리소스입니다."),
    WAITING_NOT_ALLOWED_FOR_OWN_RESERVATION(HttpStatus.CONFLICT, "본인이 예약한 시간에는 대기를 신청할 수 없습니다."),
    UNCHANGED_RESERVATION(HttpStatus.CONFLICT, "기존 예약과 같은 날짜·시간으로는 변경할 수 없습니다."),
    RESOURCE_IN_USE(HttpStatus.CONFLICT, "예약이 존재하는 리소스는 삭제할 수 없습니다."),
    PAYMENT_CANCELLATION_REQUIRED(HttpStatus.CONFLICT, "결제가 완료된 예약은 결제 취소 후 삭제할 수 있습니다."),
    PENDING_RESERVATION_LOCKED(HttpStatus.CONFLICT, "결제 대기 중인 예약은 변경할 수 없습니다."),
    PAYMENT_RETRY_NOT_ALLOWED(HttpStatus.CONFLICT, "결제를 다시 시도할 수 없는 예약입니다."),
    PAYMENT_CHECKOUT_NOT_ALLOWED(HttpStatus.CONFLICT, "결제를 진행할 수 없는 상태입니다."),
    TEMPORARY_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "요청을 처리하지 못했습니다. 잠시 후 다시 시도해주세요."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다.");

    private final HttpStatus status;
    private final String detail;

    ErrorCode(HttpStatus status, String detail) {
        this.status = status;
        this.detail = detail;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }
}
