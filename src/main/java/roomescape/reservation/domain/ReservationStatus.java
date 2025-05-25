package roomescape.reservation.domain;

public enum ReservationStatus {

    RESERVED("예약"),
    EXPIRED("만료");

    private final String label;

    ReservationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
