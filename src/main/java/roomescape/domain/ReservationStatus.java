package roomescape.domain;

public enum ReservationStatus {
    RESERVED("예약"),
    WAITING("대기"),
    ;

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
