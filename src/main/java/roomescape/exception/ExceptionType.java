package roomescape.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.springframework.http.HttpStatus;

public enum ExceptionType {
    EMPTY_NAME(BAD_REQUEST, "이름은 필수 값입니다.",
            "Name is missing in the request."),
    EMPTY_TIME(BAD_REQUEST, "시작 시간은 필수 값입니다.",
            "StartAt is missing in the request."),
    EMPTY_DATE(BAD_REQUEST, "날짜는 필수값 입니다.",
            "Date is missing in the request."),
    EMPTY_THEME(BAD_REQUEST, "테마는 필수값 입니다.",
            "Theme is missing in the request."),
    EMPTY_DESCRIPTION(BAD_REQUEST, "테마 설명은 필수값 입니다.",
            "Theme description is missing in the request."),
    EMPTY_THUMBNAIL(BAD_REQUEST, "테마 썸네일은 필수값 입니다.",
            "Theme thumbnail is missing in the request."),
    EMPTY_MEMBER(BAD_REQUEST, "사용자 정보는 필수값 입니다.",
            "Member information is missing in the request."),

    PAST_TIME_RESERVATION(BAD_REQUEST, "이미 지난 시간에 예약할 수 없습니다.",
            "Attempted to reserve a past time -> pastTime = %s"),

    DUPLICATE_RESERVATION(BAD_REQUEST, "같은 시간에 이미 예약이 존재합니다.",
            "Duplicate reservation found for the same time. -> date = %s, themeId = %s, timeId = %s"),
    DUPLICATE_WAITING_RESERVATION(BAD_REQUEST, "같은 시간에 이미 예약 혹은 예약 대기가 존재합니다.",
            "Duplicate reservation or waiting list found for the same time. -> memberId = %s, date = %s, themeId = %s, timeId = %s"),
    DUPLICATE_RESERVATION_TIME(BAD_REQUEST, "이미 예약시간이 존재합니다.",
            "Reservation time already exists. -> startAt = %s"),
    DUPLICATE_THEME(BAD_REQUEST, "이미 동일한 테마가 존재합니다.",
            "Duplicate theme found. -> themeName = %s"),

    DELETE_USED_TIME(BAD_REQUEST, "예약이 존재하는 시간은 삭제할 수 없습니다.",
            "Cannot delete time with existing reservations. -> timeId = %s"),
    DELETE_USED_THEME(BAD_REQUEST, "예약이 존재하는 테마는 삭제할 수 없습니다.",
            "Cannot delete theme with existing reservations. -> themeId = %s"),

    NOT_FOUND_RESERVATION_TIME(BAD_REQUEST, "존재하지 않는 시간입니다.",
            "Reservation time not found. -> timeId = %s"),
    NOT_FOUND_THEME(BAD_REQUEST, "존재하지 않는 테마입니다.",
            "Theme not found. -> themeId = %s"),
    NOT_FOUND_MEMBER_BY_ID(BAD_REQUEST, "존재하지 않는 사용자입니다.",
            "Member not found. -> memberId = %s"),
    NOT_FOUND_MEMBER_BY_EMAIL(BAD_REQUEST, "존재하지 않는 이메일입니다.",
            "Member not found. -> memberEmail = %s"),
    NOT_FOUND_ROLE(BAD_REQUEST, "존재하지 않는 권한 정보입니다.",
            "Role not found."),

    WRONG_PASSWORD(BAD_REQUEST, "잘못된 비밀번호입니다.",
            "Incorrect password provided. -> password = %s"),
    REQUIRED_LOGIN(UNAUTHORIZED, "로그인이 필요합니다.",
            "Login is required."),
    PERMISSION_DENIED(FORBIDDEN, "접근 권한이 없습니다.",
            "Access denied due to insufficient permissions. -> role = %s"),

    INVALID_DATE_TIME_FORMAT(BAD_REQUEST, "해석할 수 없는 날짜, 시간 포맷입니다.",
            "Invalid date or time format."),
    NO_QUERY_PARAMETER(BAD_REQUEST, "필수 검색 조건이 누락되었습니다. 요청을 다시 확인해 주세요",
            "Missing required query parameter."),
    UN_EXPECTED_ERROR(INTERNAL_SERVER_ERROR, "예상치 못한 오류입니다. 서버 관계자에게 문의하세요.",
            "Unexpected error occurred.");

    private final HttpStatus status;
    private final String message;
    private final String logMessage;

    ExceptionType(HttpStatus status, String message, String logMessage) {
        this.status = status;
        this.message = message;
        this.logMessage = logMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getLogMessage() {
        return logMessage;
    }
}
