package roomescape.support.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ReservationDateErrorCode implements ErrorCode {
    RESERVATION_DATE_NOT_EXIST(HttpStatus.NOT_FOUND,
        "지정한 식별자에 해당하는 예약 날짜 엔티티를 찾을 수 없습니다.", "요청한 날짜 ID의 유효성 및 DB 존재 여부를 확인하십시오."),
    RESERVATION_DATE_IN_USE(HttpStatus.CONFLICT,
        "외래 키 제약 조건으로 인해 삭제가 불가능합니다. (해당 날짜를 참조하는 예약 존재)", "해당 날짜와 연관된 예약 엔티티들을 먼저 처리하십시오."),
    INVALID_RESERVATION_DATE(HttpStatus.BAD_REQUEST,
        "예약 날짜 데이터가 유효하지 않거나 누락되었습니다.", "playDay 필드의 데이터 형식(yyyy-MM-dd) 및 유효성을 확인하십시오."),
    RESERVATION_DATE_DUPLICATED(HttpStatus.CONFLICT,
        "동일한 날짜 값을 가진 엔티티가 이미 존재합니다.", "데이터베이스의 중복 데이터 여부를 확인하십시오."),
    TODAY_NOT_MODIFIED(HttpStatus.BAD_REQUEST,
        "당일 예약은 수정 및 취소가 불가능합니다.", "예약일이 오늘 이후인 예약만 변경할 수 있습니다."),
    PAST_DATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST,
        "과거 예약은 수정 및 취소가 불가능합니다.", "현재 시스템 날짜 및 시각과 요청 날짜 및 시각 데이터를 확인하십시오."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String action;

    ReservationDateErrorCode(HttpStatus httpStatus, String message, String action) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.action = action;
    }

    @Override
    public String getCode() {
        return name();
    }
}
