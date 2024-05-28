package roomescape.exception;

public enum ExceptionDomainType {
    RESERVATION("예약"),
    WAITING("예약 대기"),
    RESERVATION_TIME("예약 시간"),
    THEME("테마"),
    MEMBER("멤버");
    private final String message;

    ExceptionDomainType(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
