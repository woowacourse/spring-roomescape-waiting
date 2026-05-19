package roomescape.date.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorInformation;

@Getter
@AllArgsConstructor
public enum ReservationDateErrorInformation implements ErrorInformation {

    ID_IS_NULL(HttpStatus.BAD_REQUEST, "RES_DATE_001", "예약날짜 ID가 누락되었습니다."),
    DATE_IS_NULL(HttpStatus.BAD_REQUEST, "RES_DATE_002", "예약 날짜가 누락되었습니다."),
    PAST_DATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "RES_DATE_003", "과거 날짜는 등록할 수 없습니다."),

    DATE_NOT_FOUND(HttpStatus.NOT_FOUND, "RES_DATE_004", "예약 날짜를 찾을 수 없습니다."),
    DATE_ALREADY_EXISTS(HttpStatus.CONFLICT, "RES_DATE_005", "이미 등록된 예약 날짜입니다."),
    DATE_STATUS_UPDATE_FAILED(HttpStatus.BAD_REQUEST, "RES_DATE_006", "예약 날짜 상태를 변경할 수 없습니다."),
    INACTIVE_DATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "RES_DATE_007", "해당 날짜는 비활성화 되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;

}
