package roomescape.domain.reservation;

public enum ReservationState {
    CONFIRMED("예약"),
    WAITING("예약 대기");

    private final String title;

    ReservationState(final String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
