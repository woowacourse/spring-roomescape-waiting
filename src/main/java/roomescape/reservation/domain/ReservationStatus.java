package roomescape.reservation.domain;

public enum ReservationStatus {
    BOOKED("예약"),
    ;

    private String status;

    ReservationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
