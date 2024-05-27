package roomescape.domain;

public enum WaitingStatus {
    WAITING("예약대기"),
    REJECTED("승인거절");

    private final String message;

    WaitingStatus(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean doesNotRejected() {
        return this != REJECTED;
    }
}
