package roomescape.domain.reservation;

public enum ReservationStatus {
    RESERVED("예약"),
    WAITING("대기");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isReserved() {
        return this == RESERVED;
    }
}
