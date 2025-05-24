package roomescape.service.response;

public enum ReservationStatus {
    예약("예약"),
    대기("예약 대기");

    private final String display;

    ReservationStatus(final String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
