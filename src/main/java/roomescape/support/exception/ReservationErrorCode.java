package roomescape.support.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ReservationErrorCode implements ErrorCode {
    INVALID_RESERVATION_NAME(HttpStatus.BAD_REQUEST,
        "예약자 성명 데이터가 유효하지 않습니다.", "요청 바디의 name 필드 유효성 제약 조건을 확인하십시오."),
    INVALID_RESERVATION_DATE(HttpStatus.BAD_REQUEST,
        "예약 날짜 식별자 혹은 데이터가 누락되었습니다.", "dateId 필드 포함 여부 및 데이터 형식을 확인하십시오."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND,
        "지정한 식별자에 해당하는 예약 엔티티를 찾을 수 없습니다.", "요청한 예약 ID의 유효성 및 DB 존재 여부를 확인하십시오."),
    RESERVATION_CANNOT_CANCEL(HttpStatus.BAD_REQUEST,
        "비즈니스 제약 조건에 의해 취소가 불가능한 상태입니다. (당일 혹은 과거 예약)",
        "예약 취소 정책(방문 전날 자정) 준수 여부 및 도메인 상태를 확인하십시오."),
    RESERVATION_CANNOT_UPDATE(HttpStatus.BAD_REQUEST,
        "비즈니스 제약 조건에 의해 수정이 불가능한 상태입니다. (당일 혹은 과거 예약)",
        "예약 수정 정책(방문 전날 자정) 준수 여부 및 도메인 상태를 확인하십시오."),
    RESERVATION_NOT_CHANGED(HttpStatus.BAD_REQUEST,
        "변경할 예약 날짜와 시간이 기존 예약과 동일합니다.", "기존 예약과 다른 날짜 또는 시간을 선택하십시오."),
    RESERVATION_DUPLICATED(HttpStatus.CONFLICT,
        "동일한 시간대에 중복된 예약 엔티티가 존재합니다.", "데이터베이스의 예약 현황을 확인하고 중복 요청 여부를 검토하십시오.");

    private final HttpStatus httpStatus;
    private final String message;
    private final String action;

    ReservationErrorCode(HttpStatus httpStatus, String message, String action) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.action = action;
    }

    @Override
    public String getCode() {
        return name();
    }
}
