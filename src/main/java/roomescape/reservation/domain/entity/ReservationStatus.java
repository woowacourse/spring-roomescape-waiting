package roomescape.reservation.domain.entity;

public enum ReservationStatus {

    CONFIRMATION("예약"),
    WAITING("예약대기")
    ;

    private final String statusName;

    ReservationStatus(String statusName) {
        this.statusName = statusName;
    }

    public String getStatusName() {
        return statusName;
    }

    public boolean isNotWaiting() {
        return !this.equals(WAITING);
    }
}
