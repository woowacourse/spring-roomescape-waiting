package roomescape.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WaitingErrorCode implements ErrorCode {

    WAITING_NOT_FOUND(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),
    WAITING_ID_REQUIRED(HttpStatus.BAD_REQUEST, "예약을 식별할 값이 비어있습니다."),
    WAITING_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "예약자 이름을 입력해 주세요."),
    WAITING_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "예약 날짜를 선택해 주세요."),
    WAITING_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "예약 시간을 선택해 주세요."),
    WAITING_THEME_REQUIRED(HttpStatus.BAD_REQUEST, "예약 테마를 선택해 주세요."),
    WAITING_TIME_INVALID(HttpStatus.BAD_REQUEST, "존재하지 않는 예약 시간입니다."),
    WAITING_THEME_INVALID(HttpStatus.BAD_REQUEST, "존재하지 않는 예약 테마입니다."),
    WAITING_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 예약된 시간입니다. 다른 시간을 선택해 주세요."),
    WAITING_CREATE_IN_PAST(HttpStatus.BAD_REQUEST, "지난 일정으로 예약할 수 없습니다."),
    WAITING_MODIFY_IN_PAST(HttpStatus.BAD_REQUEST, "지난 일정의 예약은 수정 및 취소할 수 없습니다."),
    WAITING_ALREADY_PAST(HttpStatus.BAD_REQUEST, "이미 지난 예약은 취소할 수 없습니다."),
    WAITING_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "수정할 수 있는 권한이 없습니다."),
    WAITING_NOT_EXIST_RESERVATION(HttpStatus.BAD_REQUEST, "예약이 존재하지 않으면, 대기요청을 할 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
