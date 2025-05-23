package roomescape.reservation.entity;

public enum Status {
    RESERVATION("예약"),
    WAITING("예약대기");

    private final String text;

    Status(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
