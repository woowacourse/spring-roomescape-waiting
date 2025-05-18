package roomescape.reservation.domain.reservation;

import lombok.Getter;

@Getter
public enum ReservationStatus {

    RESERVED("예약");

    private final String displayName;

    ReservationStatus(final String displayName) {
        this.displayName = displayName;
    }
}
