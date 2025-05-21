package roomescape.reservation.domain.reservation;

import lombok.Getter;

@Getter
public enum ReservationStatus {

    BOOKED("예약"),
    WAITING("대기");

    private final String displayName;

    ReservationStatus(final String displayName) {
        this.displayName = displayName;
    }
}
