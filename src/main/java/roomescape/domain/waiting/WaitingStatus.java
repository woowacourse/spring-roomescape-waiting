package roomescape.domain.waiting;

public enum WaitingStatus {
    WAITING("대기 중");

    private final String status;

    WaitingStatus(String status) {
        this.status = status;
    }

    public boolean isWaiting() {
        return this == WAITING;
    }

    public String getStatus() {
        return status;
    }
}
