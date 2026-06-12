package roomescape.common.exception.code;

import org.springframework.http.HttpStatus;

public enum ReservationErrorCode implements ErrorCode {
    UNAUTHORIZED_ACCESS("접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND("존재하지 않는 예약입니다.", HttpStatus.NOT_FOUND),
    DUPLICATE("동일한 날짜, 시간, 테마에 이미 예약이 존재합니다.", HttpStatus.CONFLICT),
    CANNOT_CANCEL("예약 시간 24시간 전부터는 취소할 수 없습니다", HttpStatus.UNPROCESSABLE_ENTITY);

    private final String message;
    private final HttpStatus httpStatus;

    ReservationErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
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
