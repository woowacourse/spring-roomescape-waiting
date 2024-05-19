package roomescape.domain;

public enum ReservationStatus {

    RESERVATION("예약"),
    WAITING("대기");

    private final String status;

    ReservationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
