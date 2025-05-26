package roomescape.waiting.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorCode;

@AllArgsConstructor
public enum WaitingErrorCode implements ErrorCode {
    SLOT_NOT_RESERVED(HttpStatus.BAD_REQUEST, "바로 예약 가능한 슬롯입니다."),
    WAITING_NOT_FOUND(HttpStatus.NOT_FOUND, "예약 대기를 찾을 수 없습니다."),
    NOT_FIRST_WAITING(HttpStatus.BAD_REQUEST, "우선순위가 낮은 예약 대기입니다."),
    DUPLICATED_WAITING(HttpStatus.CONFLICT, "해당 슬롯에 이미 사용자의 예약/대기가 존재합니다"),
    TOO_MANY_WAITING(HttpStatus.CONFLICT, "해당 슬롯에 대기 인원이 가득 찼습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
