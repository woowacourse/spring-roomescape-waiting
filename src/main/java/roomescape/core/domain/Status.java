package roomescape.core.domain;

import java.util.Arrays;

public enum Status {
    BOOKED("예약"),
    STANDBY("예약대기");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    public String waitingRankStatus(final Integer rank) {
        return rank + "번째 " + this.getValue();
    }

    public static Status findStatus(final String value) {
        return Arrays.stream(values()).filter(v -> v.getValue().equals(value)).findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public String getValue() {
        return value;
    }
}
