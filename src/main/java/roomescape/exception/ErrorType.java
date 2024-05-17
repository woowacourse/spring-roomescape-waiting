package roomescape.exception;

public enum ErrorType {
    SECURITY_EXCEPTION("SECURITY-001", "로그인이 필요합니다."),

    MEMBER_NOT_FOUND("AUTH-001", "해당 유저를 찾을 수 없습니다."),
    INVALID_TOKEN("AUTH-002", "유효하지 않은 토큰입니다."),
    TOKEN_PAYLOAD_EXTRACTION_FAILURE("AUTH-003", "토큰 페이로드 추출에 실패했습니다"),


    MISSING_REQUIRED_VALUE_ERROR("COMMON-001", "필수 요청값이 누락되었습니다."),
    NOT_ALLOWED_PERMISSION_ERROR("COMMON-002", "허용되지 않은 권한입니다."),
    INVALID_REQUEST_ERROR("COMMON-003", "올바르지 않은 데이터 요청입니다."),


    NAME_FORMAT_ERROR("USER-001", "올바르지 않은 이름 입력 양식입니다."),
    EMAIL_FORMAT_ERROR("USER-002", "올바르지 않은 이메일 입력 양식입니다."),
    DUPLICATED_EMAIL_ERROR("USER-003", "중복된 이메일입니다."),

    DUPLICATED_RESERVATION_ERROR("RESERVATION-001", "중복된 예약입니다."),
    DUPLICATED_RESERVATION_TIME_ERROR("RESERVATION-002", "중복된 예약 시간입니다."),
    MEMBER_RESERVATION_NOT_FOUND("RESERVATION-003", "해당 ID에 대응되는 사용자 예약이 없습니다."),
    RESERVATION_NOT_DELETED("RESERVATION-004", "예약이 존재하여 삭제할 수 없습니다."),
    NOT_A_RESERVATION_MEMBER("RESERVATION-005", "예약자가 아닙니다."),

    THEME_NOT_FOUND("THEME-001", "해당 ID에 대응되는 테마가 없습니다."),

    RESERVATION_TIME_NOT_FOUND("RESERVATION-TIME-001", "해당 ID에 대응되는 예약 시간이 없습니다."),

    UNEXPECTED_SERVER_ERROR("SERVER-001", "서버 관리자에게 문의하세요.");

    private final String errorCode;
    private final String message;

    ErrorType(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
