package roomescape.reservation.domain;

public enum ReservationStatus {
    SUCCESS("예약"),
    CANCEL("취소"),
    WAIT("대기"),
    ;

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
