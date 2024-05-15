package roomescape.domain.reservation;

public enum ReservationStatus {

    WAITING("예약 대기"),
    RESERVED("예약");

    private final String name;

    ReservationStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
