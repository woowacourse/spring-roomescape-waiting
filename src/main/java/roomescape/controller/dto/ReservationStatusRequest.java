package roomescape.controller.dto;

import roomescape.domain.reservation.ReservationStatus;

public enum ReservationStatusRequest {
    BOOKED,
    WAIT;

    public ReservationStatus toDomain() {
        return ReservationStatus.valueOf(this.name());
    }
}
