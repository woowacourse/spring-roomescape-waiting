package roomescape.core.domain;

public enum Status {
    BOOKED("예약"),
    STANDBY("예약대기");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    public String waitingRankStatus(Integer rank) {
        return rank + "번째 " + this.getValue();
    }

    public String getValue() {
        return value;
    }
}
