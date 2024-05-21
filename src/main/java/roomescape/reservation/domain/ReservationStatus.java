package roomescape.reservation.domain;

public enum ReservationStatus {
    BOOKED("예약"), WAITING("대기")
    ;

    private String status;

    ReservationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public boolean isWaiting() {
        return this == WAITING;
    }
}
