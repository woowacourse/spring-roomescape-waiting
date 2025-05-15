package roomescape.common.exception.message;

public enum ReservationTimeExceptionMessage {
    DUPLICATE_TIME("이미 해당 시간이 존재합니다"),
    RESERVED_TIME("이미 예약된 시간은 삭제할 수 없습니다");

    private final String message;

    ReservationTimeExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
