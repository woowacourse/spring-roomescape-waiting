package roomescape.domain.reservation;

public enum ReservationStatus {
    COMPLETE("예약 완료");

    private final String status;

    ReservationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
