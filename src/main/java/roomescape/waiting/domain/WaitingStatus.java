package roomescape.waiting.domain;

public enum WaitingStatus {
    PENDING("예약 대기"),
    APPROVED("예약 대기 승인"),
    DENIED("예약 대기 거절");

    private final String label;

    WaitingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
