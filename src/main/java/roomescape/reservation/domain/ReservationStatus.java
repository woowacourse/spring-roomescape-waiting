package roomescape.reservation.domain;

public enum ReservationStatus {

    CURRENT("예약"), WAITING("%d번째 예약대기");

    private final String title;

    ReservationStatus(final String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
