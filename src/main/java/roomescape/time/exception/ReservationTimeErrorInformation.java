package roomescape.time.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorInformation;

@Getter
@AllArgsConstructor
public enum ReservationTimeErrorInformation implements ErrorInformation {

    ID_IS_NULL(HttpStatus.BAD_REQUEST, "RES_TIME_001", "예약 시간 ID가 누락되었습니다."),
    START_AT_IS_NULL(HttpStatus.BAD_REQUEST, "RES_TIME_002", "예약 시작 시간이 누락되었습니다."),

    TIME_NOT_FOUND(HttpStatus.NOT_FOUND, "RES_TIME_003", "예약 시간을 찾을 수 없습니다."),
    TIME_ALREADY_EXISTS(HttpStatus.CONFLICT, "RES_TIME_004", "이미 등록된 예약 시간입니다."),
    TIME_STATUS_UPDATE_FAILED(HttpStatus.BAD_REQUEST, "RES_TIME_005", "등록된 시간 활성화/비활성화 상태 변경에 실패했습니다."),
    INACTIVE_TIME_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "RES_TIME_006", "해당 시간은 비활성화 되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;

}
