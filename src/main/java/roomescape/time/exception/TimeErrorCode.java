package roomescape.time.exception;

public enum TimeErrorCode {
    TIME_NOT_FOUND("시간이 존재하지 않습니다."),
    DUPLICATE_TIME("이미 존재하는 시간입니다."),
    TIME_IN_USE("해당 예약 시간에 예약이 존재합니다."),
    INVALID_START_AT("시작 시간이 유효하지 않습니다."),
    INVALID_FORMAT("예약 시간 요청 형식이 유효하지 않습니다.");

    private final String message;

    TimeErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
