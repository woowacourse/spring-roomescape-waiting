package roomescape.reservationTime.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorCode;

@AllArgsConstructor
public enum TimeErrorCode implements ErrorCode {
    TIME_NOT_FOUND(HttpStatus.NOT_FOUND, "시간을 찾을 수 없습니다."),
    TIME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 시간입니다."),
    USING_TIME(HttpStatus.BAD_REQUEST, "예약에 사용중인 시간입니다.");

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
