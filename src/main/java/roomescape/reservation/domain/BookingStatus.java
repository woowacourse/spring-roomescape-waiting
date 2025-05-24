package roomescape.reservation.domain;

import lombok.Getter;

@Getter
public enum BookingStatus {

    CONFIRMED("예약"),
    WAITING("대기");

    private final String description;

    BookingStatus(final String description) {
        this.description = description;
    }
}
