package roomescape.reservation.domain;

public enum ReservationLogStatus {

    RESERVED("예약"),
    WAITING("예약대기");

    private final String label;

    ReservationLogStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
