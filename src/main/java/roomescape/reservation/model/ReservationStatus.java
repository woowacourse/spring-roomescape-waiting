package roomescape.reservation.model;

import java.util.Arrays;

public enum ReservationStatus {
    PENDING("대기"),
    RESERVATION("예약"),
    END("완료"),
    ;

    private final String description;

    ReservationStatus(final String description) {
        this.description = description;
    }

    public static ReservationStatus of(final String value) {
        return Arrays.stream(ReservationStatus.values())
                .filter(status -> status.name().equals(value))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("일치하는 예약 상태가 존재하지 않습니다."));
    }

    public String getDescription() {
        return description;
    }
}
