package roomescape.reservation.domain;

public enum ReservationStatus {

    RESERVED("예약"), WAITING("예약대기");

    private final String name;

    ReservationStatus(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
