package roomescape.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    COMMON_BAD_REQUEST("COMMON_400", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    COMMON_UNAUTHORIZED("COMMON_401", "권한이 없습니다.", HttpStatus.UNAUTHORIZED),

    THEME_NAME_BLANK("THEME_400_1", "테마 이름은 비거나 공백일 수 없습니다.", HttpStatus.BAD_REQUEST),
    THEME_DESCRIPTION_BLANK("THEME_400_2", "테마 설명은 비거나 공백일 수 없습니다.", HttpStatus.BAD_REQUEST),
    THEME_THUMBNAIL_URL_BLANK("THEME_400_3", "테마 썸네일 URL은 비거나 공백일 수 없습니다.", HttpStatus.BAD_REQUEST),
    ALREADY_EXISTS_THEME("THEME_409_1", "존재하는 테마는 추가할 수 없습니다.", HttpStatus.CONFLICT),
    UNALLOWED_DELETE_EXISTS_THEME("THEME_409_2", "사용중인 테마는 삭제할 수 없습니다.", HttpStatus.CONFLICT),

    TIME_START_AT_NULL("TIME_400_1", "예약 시간은 null일 수 없습니다.", HttpStatus.BAD_REQUEST),
    ALREADY_EXISTS_TIME("TIME_409_1", "이미 존재하는 시간은 저장할 수 없습니다.", HttpStatus.CONFLICT),
    UNALLOWED_DELETE_RESERVED_TIME("TIME_409_2", "예약중인 시간은 삭제할 수 없습니다.", HttpStatus.CONFLICT),

    PAST_DATE_RESERVATION("RESERVATION_400_1", "지난 날짜/시간으로 예약할 수 없습니다.", HttpStatus.BAD_REQUEST),
    RESERVATION_NAME_BLANK("RESERVATION_400_2", "예약자 이름은 비거나 공백일 수 없습니다.", HttpStatus.BAD_REQUEST),
    RESERVATION_NAME_TOO_LONG("RESERVATION_400_3", "예약자 이름은 255자를 초과할 수 없습니다.", HttpStatus.BAD_REQUEST),
    RESERVATION_DATE_NULL("RESERVATION_400_4", "예약 날짜는 null일 수 없습니다.", HttpStatus.BAD_REQUEST),
    RESERVATION_TIME_NULL("RESERVATION_400_5", "예약 시간은 null일 수 없습니다.", HttpStatus.BAD_REQUEST),
    RESERVATION_THEME_NULL("RESERVATION_400_6", "예약 테마는 null일 수 없습니다.", HttpStatus.BAD_REQUEST),
    UNALLOWED_DELETE_PAST_RESERVATION("RESERVATION_400_7", "이미 지난 예약은 취소할 수 없습니다.", HttpStatus.BAD_REQUEST),
    RESERVATION_WAITING_NULL("RESERVATION_400_8", "예약 대기는 null일 수 없습니다.", HttpStatus.BAD_REQUEST),
    UNALLOWED_CHANGE_RESERVATION_THEME("RESERVATION_400_9", "예약의 테마는 변경할 수 없습니다. 예약 취소 후 다시 예약해주세요.", HttpStatus.BAD_REQUEST),
    DUPLICATE_RESERVATION("RESERVATION_409_1", "해당 시간은 이미 예약이 마감되었습니다. 다른 시간을 선택해주세요.", HttpStatus.CONFLICT),
    ALREADY_EXISTS_RESERVATION("RESERVATION_409_2", "이미 예약중인 시간입니다.", HttpStatus.CONFLICT);

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
