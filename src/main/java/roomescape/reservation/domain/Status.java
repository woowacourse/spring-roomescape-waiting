package roomescape.reservation.domain;

public enum Status {
    RESERVED("예약"),
    WAITING("대기");

    private final String status;

    Status(final String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
