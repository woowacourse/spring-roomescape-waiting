package roomescape.reservation.domain;

public enum ReservationStatus {
    RESERVED(0),
    WAITING(1);

    private final int priority;

    ReservationStatus(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }
}
