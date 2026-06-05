package roomescape.support.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum WaitingReservationErrorCode implements ErrorCode {
    INVALID_RESERVATION_NAME(HttpStatus.BAD_REQUEST,
        "예약자 성명 데이터가 유효하지 않습니다.", "요청 바디의 name 필드 유효성 제약 조건을 확인하십시오."),
    INVALID_CREATED_AT(HttpStatus.BAD_REQUEST,
        "생성 시간이 유효하지 않습니다.", "요청 바디의 createdAt 필드 유효성 제약 조건을 확인하십시오."),
    AVAILABLE_SLOT_NOT_WAITABLE(HttpStatus.CONFLICT,
        "예약 가능한 시간에는 대기를 신청할 수 없습니다.", "예약된 날짜, 시간, 테마에만 대기를 신청하십시오."),
    PAST_TIME_NOT_ALLOWED(HttpStatus.BAD_REQUEST,
        "과거 시간에는 예약 대기를 신청할 수 없습니다.", "예약 대기 날짜와 시간이 현재 이후인지 확인하십시오."),
    DUPLICATE_WAITING_RESERVATION(HttpStatus.CONFLICT,
        "중복으로 대기 신청을 할 수 없습니다.", "동일한 이름으로 신청된 예약 대기가 있는지 확인하세요.");

    private final HttpStatus httpStatus;
    private final String message;
    private final String action;

    WaitingReservationErrorCode(HttpStatus httpStatus, String message, String action) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.action = action;
    }

    @Override
    public String getCode() {
        return name();
    }
}
