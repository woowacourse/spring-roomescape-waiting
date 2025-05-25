package roomescape.domain;

public enum ReservationStatus {
    ACCEPTED("예약 확정"),
    PENDING("예약 대기"),
    ;

    public final String description;

    ReservationStatus(String description) {
        this.description = description;
    }
}
