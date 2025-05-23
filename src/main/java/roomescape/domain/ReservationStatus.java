package roomescape.domain;

public enum ReservationStatus {

    RESERVED("예약"),
    WAITING("예약 대기");

    private final String name;

    ReservationStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
