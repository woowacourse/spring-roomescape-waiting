package roomescape.registration.domain.reservation.domain;

public enum ReservationStatus {

    RESERVED("예약"),
    WAITING("번째 예약 대기");

    private final String status;

    ReservationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
