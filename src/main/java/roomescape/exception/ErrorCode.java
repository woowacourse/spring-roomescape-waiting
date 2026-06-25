package roomescape.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    COMMON_BAD_REQUEST("COMMON_400", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    COMMON_UNAUTHORIZED("COMMON_401", "권한이 없습니다.", HttpStatus.UNAUTHORIZED),
    COMMON_SERVER_ERROR("COMMON_500", "500 서버 에러", HttpStatus.INTERNAL_SERVER_ERROR),

    PAYMENT_AMOUNT_MISMATCH("PAYMENT_400", "결제 금액이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_GATEWAY_CONNECTION_FAILED("PAYMENT_503", "결제 승인 서버에 연결하지 못했습니다. 잠시 후 다시 시도해주세요.", HttpStatus.SERVICE_UNAVAILABLE),
    PAYMENT_CONFIRM_RESULT_UNKNOWN("PAYMENT_504", "결제 승인 결과를 확인하지 못했습니다. 실제 결제가 승인되었을 수 있으니 결제 내역을 확인해주세요.", HttpStatus.GATEWAY_TIMEOUT),

    THEME_NAME_TOO_LONG("THEME_400", "테마 이름은 255자 이하여야합니다.", HttpStatus.BAD_REQUEST),
    THEME_DESCRIPTION_TOO_LONG("THEME_400", "테마 설명은 255자 이하여야합니다.", HttpStatus.BAD_REQUEST),
    THEME_THUMBNAIL_TOO_LONG("THEME_400", "테마 썸네일 URL은 255자 이하여야합니다.", HttpStatus.BAD_REQUEST),
    ALREADY_EXISTS_THEME("THEME_409", "존재하는 테마는 추가할 수 없습니다.", HttpStatus.CONFLICT),
    UNALLOWED_DELETE_EXISTS_THEME("THEME_409", "사용중인 테마는 삭제할 수 없습니다.", HttpStatus.CONFLICT),

    ALREADY_EXISTS_TIME("TIME_409", "이미 존재하는 시간은 저장할 수 없습니다.", HttpStatus.CONFLICT),
    UNALLOWED_DELETE_RESERVED_TIME("TIME_409", "예약중인 시간은 삭제할 수 없습니다.", HttpStatus.CONFLICT),

    RESERVATION_NAME_TOO_LONG("RESERVATION_400", "예약자 이름은 255자를 초과할 수 없습니다.", HttpStatus.BAD_REQUEST),
    PAST_DATE_RESERVATION("RESERVATION_400", "지난 날짜/시간으로 예약할 수 없습니다.", HttpStatus.BAD_REQUEST),
    UNALLOWED_UPDATE_PAST_RESERVATION("RESERVATION_400", "이미 지난 예약은 수정할 수 없습니다. ", HttpStatus.BAD_REQUEST),
    UNALLOWED_DELETE_PAST_RESERVATION("RESERVATION_400", "이미 지난 예약은 취소할 수 없습니다. ", HttpStatus.BAD_REQUEST),
    UNALLOWED_CHANGE_RESERVATION_THEME("RESERVATION_400", "예약의 테마는 변경할 수 없습니다. 에약 취소 후 다시 예약해주세요.", HttpStatus.BAD_REQUEST),
    RESERVATION_STATUS_UNAVAILABLE("RESERVATION_400", "예약 상태가 아닙니다.", HttpStatus.BAD_REQUEST),
    RESERVATION_DATE_UNAVAILABLE("RESERVATION_400", "예약 변경 가능 시간이 아닙니다.", HttpStatus.BAD_REQUEST),
    NOT_EXIST_RESERVATION("Reservation_404", "존재하지 않는 예약입니다.", HttpStatus.NOT_FOUND),
    ALREADY_EXISTS_RESERVATION("RESERVATION_409", "이미 예약중인 시간입니다.", HttpStatus.CONFLICT),

    ALREADY_PAST_RESERVATION_SLOT("RESERVATION_SLOT_400", "이미 시간이 지난 예약 슬롯입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_RESERVATION_SLOT("RESERVATION_SLOT_400", "이미 존재하는 예약 슬롯입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
