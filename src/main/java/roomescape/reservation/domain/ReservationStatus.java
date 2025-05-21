package roomescape.reservation.domain;

public enum ReservationStatus {

    RESERVED("예약");

    private final String displayName;

    ReservationStatus(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
