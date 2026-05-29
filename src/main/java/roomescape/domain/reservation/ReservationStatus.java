package roomescape.domain.reservation;

public enum ReservationStatus {
    PENDING,
    CONFIRMED,
    REJECTED,
    CANCELLED,
    ;

    public ReservationStatus pending() {
        return PENDING;
    }

    public ReservationStatus confirm() {
        return CONFIRMED;
    }

    public ReservationStatus reject() {
        return REJECTED;
    }

    public ReservationStatus cancel() {
        return CANCELLED;
    }
}
