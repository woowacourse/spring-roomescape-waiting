package roomescape.domain;

public enum ReservationStatus {

    RESERVED("예약"),
    WAITING("예약대기");

    private final String status;

    ReservationStatus(String status) {
        this.status = status;
    }

    public boolean isReserved() {
        return RESERVED.equals(this);
    }

    public String getStatus() {
        return status;
    }
}
