package roomescape.business.model.vo;

public enum ReservationStatus {
    RESERVED("예약"),
    ;

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
