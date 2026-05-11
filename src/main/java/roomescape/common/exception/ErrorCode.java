package roomescape.common.exception;

public enum ErrorCode {
    INVALID_RESERVATION_ID("예약 id는 비어 있을 수 없습니다."),
    INVALID_RESERVATION_NAME("예약자 이름은 비어 있을 수 없습니다."),
    INVALID_RESERVATION_DATE("예약 날짜는 비어 있을 수 없습니다."),
    INVALID_RESERVATION_TIME("예약 시간은 비어 있을 수 없습니다."),
    INVALID_RESERVATION_TIME_ID("예약 시간 id는 비어 있을 수 없습니다."),
    RESERVATION_ALREADY_HAS_ID("이미 식별자가 존재하는 예약입니다."),
    RESERVATION_ALREADY_EXISTS("이미 존재하는 예약입니다."),
    RESERVATION_NOT_FOUND("존재하지 않는 예약입니다."),
    RESERVATION_TIME_ALREADY_HAS_ID("이미 id가 존재하는 예약 시간입니다."),
    RESERVATION_TIME_ALREADY_EXISTS("이미 존재하는 예약 시간입니다."),
    RESERVATION_CREATE_FAILED("예약 생성에 실패했습니다."),
    RESERVATION_TIME_NOT_FOUND("존재하지 않는 예약 시간입니다."),
    RESERVATION_TIME_CREATE_FAILED("예약 시간 생성에 실패했습니다."),
    INVALID_THEME_ID("테마 id는 비어 있을 수 없습니다."),
    INVALID_THEME_NAME("테마 이름은 비어 있을 수 없습니다."),
    INVALID_THEME_DESCRIPTION("테마 설명은 비어 있을 수 없습니다."),
    INVALID_THEME_THUMBNAIL("테마 썸네일은 비어 있을 수 없습니다."),
    INVALID_THEME("테마 정보는 비어 있을 수 없습니다."),
    THEME_ALREADY_HAS_ID("이미 id가 존재하는 테마입니다."),
    THEME_NOT_FOUND("존재하지 않는 테마입니다."),
    THEME_CREATE_FAILED("테마 생성에 실패했습니다."),
    THEME_HAS_RESERVATION("예약이 있는 테마는 삭제할 수 없습니다."),
    RESERVATION_TIME_HAS_RESERVATION("예약이 있는 시간은 삭제할 수 없습니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
