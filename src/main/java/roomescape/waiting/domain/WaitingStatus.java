package roomescape.waiting.domain;

public enum WaitingStatus {

    CURRENT("예약"), WAITING("%d번째 예약대기");

    private final String title;

    WaitingStatus(final String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
