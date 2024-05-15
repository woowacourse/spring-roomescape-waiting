package roomescape.reservation.domain;

public enum ReservationStatus {
    BOOKED("예약"),
    WAITING("예약 대기");

    private final String status;

    ReservationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
