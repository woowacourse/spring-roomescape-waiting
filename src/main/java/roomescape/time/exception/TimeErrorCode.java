package roomescape.time.exception;

import roomescape.global.exception.ErrorCode;

public enum TimeErrorCode implements ErrorCode {
    TIME_NOT_FOUND("해당 시간이 존재하지 않습니다. 화면을 새로고침한 후 다시 시도하십시오."),
    DUPLICATE_TIME("이미 등록된 시간입니다. 중복되지 않는 다른 시간을 입력하십시오."),
    TIME_IN_USE("해당 시간에 등록된 예약 데이터가 존재합니다. 예약을 먼저 취소하거나 변경하십시오.");

    private final String message;

    TimeErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
