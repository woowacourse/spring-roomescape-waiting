package roomescape.reservation.controller.dto;

public enum ReservationStatus {
    WAITING("대기"),
    CONFIRM("예약");

    private final String status;

    ReservationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
