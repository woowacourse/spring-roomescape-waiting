package roomescape.exception;

import org.springframework.http.HttpStatus;

public enum ErrorType {
    SECURITY_EXCEPTION(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 유저를 찾을 수 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_PAYLOAD_EXTRACTION_FAILURE(HttpStatus.UNAUTHORIZED, "토큰 페이로드 추출에 실패했습니다"),

    MISSING_REQUIRED_VALUE_ERROR(HttpStatus.BAD_REQUEST, "필수 요청값이 누락되었습니다."),
    NOT_ALLOWED_PERMISSION_ERROR(HttpStatus.FORBIDDEN, "허용되지 않은 권한입니다."),
    INVALID_REQUEST_ERROR(HttpStatus.BAD_REQUEST, "올바르지 않은 데이터 요청입니다."),

    NAME_FORMAT_ERROR(HttpStatus.BAD_REQUEST, "올바르지 않은 이름 입력 양식입니다."),
    EMAIL_FORMAT_ERROR(HttpStatus.BAD_REQUEST, "올바르지 않은 이메일 입력 양식입니다."),
    DUPLICATED_NAME_ERROR(HttpStatus.CONFLICT, "중복된 이름입니다."),
    DUPLICATED_EMAIL_ERROR(HttpStatus.CONFLICT, "중복된 이메일입니다."),
    DUPLICATED_RESERVATION_ERROR(HttpStatus.CONFLICT, "중복된 예약입니다."),
    DUPLICATED_RESERVATION_TIME_ERROR(HttpStatus.CONFLICT, "중복된 예약 시간입니다."),

    THEME_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 ID에 대응되는 테마가 없습니다."),
    RESERVATION_TIME_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 ID에 대응되는 예약 시간이 없습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 ID에 대응되는 예약이 없습니다."),
    MEMBER_RESERVATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 ID에 대응되는 사용자 예약이 없습니다."),

    RESERVATION_NOT_DELETED(HttpStatus.BAD_REQUEST, "예약이 존재하여 삭제할 수 없습니다."),
    NOT_A_RESERVATION_MEMBER(HttpStatus.FORBIDDEN, "예약자가 아닙니다."),

    UNEXPECTED_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 관리자에게 문의하세요.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorType(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
