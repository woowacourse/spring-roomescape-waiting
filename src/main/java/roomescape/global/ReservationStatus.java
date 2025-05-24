package roomescape.global;

public enum ReservationStatus {
    RESERVED("예약"),
    WAIT("%d 번째 예약대기");

    private final String text;

    ReservationStatus(final String text) {
        this.text = text;
    }

    public String renderText(long rank) {
        return this.text.formatted(rank);
    }
}
