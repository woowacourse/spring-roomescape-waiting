package roomescape.domain;

public enum ReservationStatus {

    BOOKING("예약"),
    PENDING("예약 대기");

    private final String name;

    ReservationStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
