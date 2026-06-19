package roomescape.feature.reservation.error.type;

import org.springframework.http.HttpStatus;
import roomescape.global.error.type.ErrorType;

public enum ReservationErrorType implements ErrorType {
    NOT_RESERVED(HttpStatus.CONFLICT, "아직 예약되지 않은 날짜, 시간, 테마입니다."),
    ALREADY_RESERVED(HttpStatus.CONFLICT, "이미 예약된 날짜, 시간, 테마입니다."),
    ALREADY_WAITING(HttpStatus.CONFLICT, "이미 대기 중인 이름, 날짜, 시간, 테마입니다."),
    FIELD_RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "조회할 자원이 존재하지 않습니다."),
    UPDATE_FIELD_RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "수정할 자원이 존재하지 않습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),
    RESERVATION_NOT_OWNER(HttpStatus.FORBIDDEN, "본인의 예약이 아닙니다."),
    RESERVATION_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "예약을 변경할 권한이 없습니다."),
    RESERVATION_CANCEL_FORBIDDEN(HttpStatus.FORBIDDEN, "예약을 취소할 권한이 없습니다."),
    PAST_RESERVATION_CREATE(HttpStatus.CONFLICT, "지난 예약은 생성할 수 없습니다"),
    PAST_RESERVATION_UPDATE(HttpStatus.CONFLICT, "지난 예약은 변경할 수 없습니다."),
    PAST_RESERVATION_CANCEL(HttpStatus.CONFLICT, "지난 예약은 취소할 수 없습니다."),
    NOT_ACTIVE_RESERVATION(HttpStatus.CONFLICT, "활성된 예약이 아닙니다."),
    ALREADY_CANCELED(HttpStatus.CONFLICT, "이미 취소된 예약입니다."),
    ALREADY_DELETED(HttpStatus.CONFLICT, "이미 삭제된 예약입니다."),
    NOT_WAITING_RESERVATION(HttpStatus.CONFLICT, "대기중인 예약이 아닙니다."),
    ALREADY_CONFIRMED_ORDER(HttpStatus.CONFLICT, "이미 주문이 확정된 예약입니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 올바르지 않습니다."),
    RESERVATION_NOT_CHANGED(HttpStatus.BAD_REQUEST, "변경할 내용이 없습니다."),
    ILLEGAL_RESERVER_NAME(HttpStatus.BAD_REQUEST, "예약자명이 부적절합니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ReservationErrorType(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public HttpStatus status() {
        return httpStatus;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public String code() {
        return name();
    }
}
