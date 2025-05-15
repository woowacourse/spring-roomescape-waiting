package roomescape.reservation.domain;

public enum ReservationStatus {
    RESERVED("예약");

    private final String status;

    ReservationStatus(final String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
