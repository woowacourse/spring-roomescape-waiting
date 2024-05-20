package roomescape.reservation.domain;

import roomescape.global.exception.ViolationException;

import java.util.Arrays;

public enum ReservationStatus {
    BOOKING("BOOKING"),
    WAITING("WAITING");

    private final String identifier;

    ReservationStatus(String identifier) {
        this.identifier = identifier;
    }

    public static ReservationStatus from(String identifier) {
        return Arrays.stream(ReservationStatus.values())
                .filter(status -> status.identifier.equals(identifier))
                .findAny()
                .orElseThrow(() -> new ViolationException("예약 상태 식별자가 올바르지 않습니다."));
    }

    public boolean isBooking() {
        return this == BOOKING;
    }

    public boolean isWaiting() {
        return this == WAITING;
    }

    public String getIdentifier() {
        return identifier;
    }
}
