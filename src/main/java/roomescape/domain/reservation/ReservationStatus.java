package roomescape.domain.reservation;

public enum ReservationStatus {

    RESERVATION("예약"),
    PREPARE("대기"),
    END("종료");

    private final String status;

    ReservationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
