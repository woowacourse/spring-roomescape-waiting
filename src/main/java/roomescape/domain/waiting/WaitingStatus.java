package roomescape.domain.waiting;

public enum WaitingStatus {
    SUCCESS("대기 성공"),
    WAITING("대기 중"),
    CANCEL("대기 취소");

    private final String status;

    WaitingStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
