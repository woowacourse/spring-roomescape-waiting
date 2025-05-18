package roomescape.reservation.domain;

import lombok.Getter;

@Getter
public enum BookingStatus {

    RESERVED("예약"),
    WAITING("대기");

    private final String value;

    BookingStatus(final String value) {
        this.value = value;
    }
}
