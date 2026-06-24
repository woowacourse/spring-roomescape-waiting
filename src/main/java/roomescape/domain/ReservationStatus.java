package roomescape.domain;

import java.util.Arrays;

public enum ReservationStatus {

    PENDING,
    CONFIRM;

    private static final String UNKNOWN_STATUS_FORMAT = "알 수 없는 예약 상태 값입니다: '%s'";

    public static ReservationStatus of(String value) {
        return Arrays.stream(values())
                .filter(reservationStatus -> reservationStatus.name().equals(value))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format(UNKNOWN_STATUS_FORMAT, value)));
    }
}
