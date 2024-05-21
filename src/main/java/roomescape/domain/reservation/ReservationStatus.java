package roomescape.domain.reservation;

import java.util.Arrays;

public enum ReservationStatus {
    COMPLETE("예약"),
    WAITING("대기");
    private final String value;

    ReservationStatus(final String value) {
        this.value = value;
    }

    public static ReservationStatus from(final String value) {
        return Arrays.stream(ReservationStatus.values())
                .filter(status -> status.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("%s 는 역할에 없습니다.", value)));
    }

    public String getValue() {
        return value;
    }
}
