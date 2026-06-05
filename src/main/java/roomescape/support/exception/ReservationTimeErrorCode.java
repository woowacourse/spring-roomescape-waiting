package roomescape.support.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ReservationTimeErrorCode implements ErrorCode {

    INVALID_RESERVATION_TIME(HttpStatus.BAD_REQUEST,
        "예약 시간 식별자 정보가 누락되었습니다.", "timeId 필드 포함 여부를 확인하십시오."),
    RESERVATION_TIME_NOT_EXIST(HttpStatus.NOT_FOUND,
        "지정한 식별자에 해당하는 예약 시간 엔티티를 찾을 수 없습니다.", "요청한 시간 ID의 유효성 및 DB 존재 여부를 확인하십시오."),
    RESERVATION_TIME_IN_USE(HttpStatus.CONFLICT,
        "외래 키 제약 조건으로 인해 삭제가 불가능합니다. (해당 시간을 참조하는 예약 존재)", "해당 시간과 연관된 예약 엔티티들을 먼저 처리하십시오."),
    RESERVATION_TIME_DUPLICATED(HttpStatus.CONFLICT,
        "동일한 시간 값을 가진 엔티티가 이미 존재합니다.", "데이터베이스의 중복 데이터 여부를 확인하십시오."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String action;

    ReservationTimeErrorCode(HttpStatus httpStatus, String message, String action) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.action = action;
    }

    @Override
    public String getCode() {
        return name();
    }
}
