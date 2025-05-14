package roomescape.reservation.domain;

import lombok.Getter;

@Getter
public enum ReservationStatus {

    CONFIRMED("예약"),
    WAITING("대기");

    private final String description;

    ReservationStatus(final String description) {
        this.description = description;
    }
}
