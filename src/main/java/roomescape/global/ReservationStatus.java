package roomescape.global;

public enum ReservationStatus {
    RESERVED("예약"),
    WAIT("대기");

    private final String text;

    ReservationStatus(final String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
