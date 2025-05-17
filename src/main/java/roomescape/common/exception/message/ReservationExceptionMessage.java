package roomescape.common.exception.message;

public enum ReservationExceptionMessage {
    TIME_BEFORE_NOW("당일의 과거 시간대로는 예약할 수 없습니다."),
    DUPLICATE_RESERVATION("이미 동일한 예약이 존재합니다.");

    private final String message;

    ReservationExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
