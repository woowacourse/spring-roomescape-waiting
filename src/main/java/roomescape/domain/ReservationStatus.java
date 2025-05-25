package roomescape.domain;

public enum ReservationStatus {
    ACCEPTED("예약 확정"),
    WAITING("예약 대기"),
    ;

    public final String description;

    ReservationStatus(String description) {
        this.description = description;
    }
}
