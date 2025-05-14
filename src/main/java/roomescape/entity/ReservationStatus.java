package roomescape.entity;

public enum ReservationStatus {
    RESERVATION("예약"),
    WAITING("예약대기");

    private final String text;

    ReservationStatus(String text){
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
