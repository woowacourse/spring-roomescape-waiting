package roomescape.common.exception;

import org.springframework.http.HttpStatus;

public enum ReservationErrorCode implements ErrorCode{
    INVALID_NAME_LENGTH("이름 길이는 1자 ~ 20자 사이여야 합니다.", HttpStatus.BAD_REQUEST),
    RESERVATION_NOT_FOUND("존재하지 않는 예약입니다. 입력을 확인해 주세요.", HttpStatus.NOT_FOUND),
    DUPLICATE_RESERVATION("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.", HttpStatus.CONFLICT),
    PAST_DATE_NOT_ALLOWED("기준 날짜는 과거일 수 없습니다. 오늘 이후 날짜를 입력해 주세요", HttpStatus.UNPROCESSABLE_ENTITY),
    PAST_RESERVATION_NOT_ALLOWED("과거 예약에 대한 조작은 불가능합니다. 오늘 이후 날짜와 시간으로 다시 시도해 주세요", HttpStatus.UNPROCESSABLE_ENTITY),
    UNAUTHORIZED_SAME_NAME("예약자명이 다릅니다.", HttpStatus.UNAUTHORIZED);

    private final String message;
    private final HttpStatus httpStatus;

    ReservationErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
