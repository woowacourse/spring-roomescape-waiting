package roomescape.reservation.domain;

public enum ReservationStatus {

    CONFIRMATION("예약"),
    WAITING("대기")
    ;

    private final String statusName;

    ReservationStatus(String statusName) {
        this.statusName = statusName;
    }

    public String getStatusName() {
        return statusName;
    }
}
