package roomescape.reservation.domain;

public enum Status {
    RESERVED("예약"),
    WAITING("대기");

    private final String displayName;

    Status(final String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
