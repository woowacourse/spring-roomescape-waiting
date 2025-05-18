package roomescape.domain;

public enum ReservationStatus {

    RESERVED("예약"),
    NOT_RESERVED("미예약")
    ;

    private final String status;

    ReservationStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return status;
    }
}
