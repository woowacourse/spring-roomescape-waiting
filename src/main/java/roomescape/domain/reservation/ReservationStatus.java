package roomescape.domain.reservation;

public enum ReservationStatus {

    BOOKED(true),
    AVAILABLE(false);

    private final boolean status;

    ReservationStatus(boolean status) {
        this.status = status;
    }

    public boolean getStatus() {
        return status;
    }
}
