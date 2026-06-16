package roomescape.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Reservation
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_404", "예약(%d번)이 존재하지 않습니다."),
    RESERVATION_ALREADY_EXIST(HttpStatus.CONFLICT, "RESERVATION_409_ALREADY_EXIST", "슬롯(%d번)을 사용중인 예약이 이미 존재합니다."),
    RESERVATION_NOT_AVAILABLE(HttpStatus.CONFLICT, "RESERVATION_409", "슬롯(%d번)은 이미 예약 또는 대기 중입니다."),
    RESERVATION_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RESERVATION_DELETE_500",
            "서버 내부 오류가 발생하였습니다. 관리자에게 문의해주세요."),
    RESERVATION_NOT_OWNED_BY_MEMBER(HttpStatus.FORBIDDEN, "RESERVATION_403_OWNER", "본인 예약(%d번)만 처리할 수 있습니다."),

    // Waiting
    WAITING_ALREADY_EXIST(HttpStatus.CONFLICT, "WAITING_409", "이미 신청한 대기가 존재합니다."),
    WAITING_NOT_ALLOWED_FOR_OWN_RESERVATION(HttpStatus.CONFLICT, "WAITING_409_OWN_RESERVATION",
            "본인이 예약한 슬롯에는 대기를 신청할 수 없습니다."),
    WAITING_NOT_FOUND(HttpStatus.NOT_FOUND, "WAITING_404", "대기(%d번)가 존재하지 않습니다."),
    WAITING_NOT_OWNED_BY_MEMBER(HttpStatus.FORBIDDEN, "WAITING_403_OWNER", "본인 대기(%d번)만 처리할 수 있습니다."),
    WAITING_TARGET_BAD_REQUEST(HttpStatus.BAD_REQUEST, "WAITING_400_TARGET_BAD_REQUEST",
            "예약 또는 대기가 존재하는 슬롯에만 대기를 신청할 수 있습니다."),

    // Slot
    SLOT_NOT_FOUND_WITH_CONDITION(HttpStatus.NOT_FOUND, "SLOT_404_WITH_CONDITION",
            "해당 조건의 슬롯 id가 존재하지 않습니다. date=%s, timeId=%d, themeId=%d"),
    SLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "SLOT_404", "슬롯(%d번)이 존재하지 않습니다."),
    SLOT_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SLOT_500", "서버 내부 오류가 발생하였습니다. 관리자에게 문의해주세요."),
    SLOT_TIME_IN_USE(HttpStatus.CONFLICT, "SLOT_TIME_409", "해당 시간(%d번)을 사용하는 일정이 있어 삭제할 수 없습니다."),
    SLOT_THEME_IN_USE(HttpStatus.CONFLICT, "SLOT_THEME_409", "해당 테마(%d번)를 사용하는 일정이 있어 삭제할 수 없습니다."),
    PAST_SLOT(HttpStatus.BAD_REQUEST, "PAST_SLOT_400", "이미 지난 예약이거나 날짜/시간은 처리할 수 없습니다."),
    SLOT_ALREADY_EXIST(HttpStatus.CONFLICT, "SLOT_409", "이미 존재하는 슬롯 입니다."),
    SLOT_IN_USE(HttpStatus.CONFLICT, "SLOT_409", "%d번 슬롯이 사용중 입니다."),

    // ReservationTime
    RESERVATIONTIME_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATIONTIME_404", "시간(%d번)이 존재하지 않습니다."),
    RESERVATIONTIME_ALREADY_EXIST(HttpStatus.CONFLICT, "RESERVATIONTIME_409", "이미 존재하는 시간대 입니다"),

    // Theme
    THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "THEME_404", "테마(%d번)이 존재하지 않습니다."),
    THEME_ALREADY_EXIST(HttpStatus.CONFLICT, "THEME_409", "이미 존재하는 테마 입니다"),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404", "회원(%d번)이 존재하지 않습니다."),

    // 요청 값
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT_400", "요청 값이 올바르지 않습니다."),

    // 인증/인가
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "LOGIN_401", "아이디 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN_403", "접근 권한이 없습니다."),

    // 서버 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR_500",
            "서버 내부 오류가 발생하였습니다. 관리자에게 문의해주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    public String formatMessage(Object... args) {
        return String.format(message, args);
    }
}
