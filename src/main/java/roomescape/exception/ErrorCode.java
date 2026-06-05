package roomescape.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Reservation
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_404", "예약(%d번)이 존재하지 않습니다."),
    RESERVATION_ALREADY_EXIST(HttpStatus.CONFLICT, "RESERVATION_409", "예약이 이미 존재합니다."),
    RESERVATION_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RESERVATION_DELETE_500", "서버 내부 오류가 발생하였습니다. 관리자에게 문의해주세요."),
    RESERVATION_UPDATE_EMPTY(HttpStatus.BAD_REQUEST, "RESERVATION_400", "수정할 예약 정보가 없습니다."),
    RESERVATION_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RESERVATION_UPDATE_500", "서버 내부 오류가 발생하였습니다. 관리자에게 문의해주세요."),
    RESERVATION_SAME_SCHEDULE(HttpStatus.CONFLICT, "RESERVATION_UPDATE_409", "기존 스케줄과 동일하여 수정할 수 없습니다."),
    RESERVATION_NOT_FOUND_AFTER_UPDATE(HttpStatus.NOT_FOUND, "RESERVATION_404_AFTER_UPDATE", "수정 후 예약(%d번)을 찾을 수 없습니다."),
    RESERVATION_NOT_OWNED_BY_MEMBER(HttpStatus.FORBIDDEN, "RESERVATION_403_OWNER", "본인 예약(%d번)만 처리할 수 있습니다."),

    // Waiting
    WAITING_ALREADY_EXIST(HttpStatus.CONFLICT, "WAITING_409", "이미 신청한 대기가 존재합니다."),
    WAITING_ON_OWN_RESERVATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "WAITING_400_OWN_RESERVATION", "본인의 예약에는 대기를 신청할 수 없습니다."),
    WAITING_TARGET_BAD_REQUEST(HttpStatus.BAD_REQUEST, "WAITING_400_TARGET_BAD_REQUEST", "해당 스케줄에 예약, 대기가 없어 대기를 신청할 수 없습니다."),
    WAITING_NOT_OWNED_BY_MEMBER(HttpStatus.FORBIDDEN, "WAITING_403_OWNER", "본인 대기(%d번)만 처리할 수 있습니다."),

    // Schedule
    SCHEDULE_NOT_FOUND_WITH_CONDITION(HttpStatus.NOT_FOUND, "SCHEDULE_404_WITH_CONDITION", "해당 조건의 스케줄 id가 존재하지 않습니다. date=%s, timeId=%d, themeId=%d"),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE_404", "스케줄(%d번)이 존재하지 않습니다."),
    SCHEDULE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SCHEDULE_500", "서버 내부 오류가 발생하였습니다. 관리자에게 문의해주세요."),
    SCHEDULE_TIME_IN_USE(HttpStatus.CONFLICT, "SCHEDULE_TIME_409", "해당 시간(%d번)을 사용하는 일정이 있어 삭제할 수 없습니다."),
    SCHEDULE_THEME_IN_USE(HttpStatus.CONFLICT, "SCHEDULE_THEME_409", "해당 테마(%d번)를 사용하는 일정이 있어 삭제할 수 없습니다."),
    PAST_SCHEDULE(HttpStatus.BAD_REQUEST, "PAST_SCHEDULE_400", "이미 지난 예약이거나 날짜/시간은 처리할 수 없습니다."),
    SCHEDULE_ALREADY_EXIST(HttpStatus.CONFLICT, "SCHEDULE_409", "이미 존재하는 스케줄 입니다."),
    SCHEDULE_IN_USE(HttpStatus.CONFLICT, "SCHEDULE_409", "%d번 스케줄이 사용중 입니다."),

    // ReservationTime
    RESERVATIONTIME_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATIONTIME_404", "시간(%d번)이 존재하지 않습니다."),
    RESERVATIONTIME_ALREADY_EXIST(HttpStatus.CONFLICT, "RESERVATIONTIME_409", "이미 존재하는 시간대 입니다"),

    // Theme
    THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "THEME_404", "테마(%d번)이 존재하지 않습니다."),
    THEME_ALREADY_EXIST(HttpStatus.CONFLICT, "THEME_409", "이미 존재하는 테마 입니다"),

    // 요청 값
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT_400", "요청 값이 올바르지 않습니다."),

    // 인증/인가
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "LOGIN_401", "아이디 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN_403", "접근 권한이 없습니다."),

    // 서버 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR_500", "서버 내부 오류가 발생하였습니다. 관리자에게 문의해주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    public String formatMessage(Object... args) {
        return String.format(message, args);
    }
}
