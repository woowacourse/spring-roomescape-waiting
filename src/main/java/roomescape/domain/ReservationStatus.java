package roomescape.domain;

public enum ReservationStatus {

    RESERVED("예약"),
    CANCELED("취소")
    ;

    private final String status;

    ReservationStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return status;
    }
}
