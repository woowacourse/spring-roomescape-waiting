package roomescape.exception;

import org.springframework.http.HttpStatus;

public enum RoomescapeErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부에서 에러가 발생했습니다."),
    DUPLICATED_RESERVATION(HttpStatus.CONFLICT, "이미 존재하는 예약입니다."),
    DUPLICATED_TIME(HttpStatus.CONFLICT, "이미 존재하는 예약 시간입니다."),
    DUPLICATED_THEME(HttpStatus.CONFLICT, "이미 존재하는 테마입니다."),
    NOT_FOUND_RESERVATION(HttpStatus.NOT_FOUND, "존재하지 않는 예약입니다."),
    NOT_FOUND_TIME(HttpStatus.NOT_FOUND, "존재하지 않는 예약 시간입니다."),
    NOT_FOUND_THEME(HttpStatus.NOT_FOUND, "존재하지 않는 테마입니다."),
    NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    ALREADY_RESERVED(HttpStatus.CONFLICT, "이미 연관된 예약이 존재합니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "적합한 권한이 아닙니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "인증 유효기간이 만료되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    RoomescapeErrorCode(HttpStatus httpStatus, String message) {
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
