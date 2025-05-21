package roomescape.presentation.dto;

import roomescape.business.domain.ReservationTime;

public record AvailableReservationTimeResponse(ReservationTimeResponse reservationTime, boolean alreadyBooked) {

    public static AvailableReservationTimeResponse from(ReservationTime reservationTime, boolean alreadyBooked) {
        return new AvailableReservationTimeResponse(ReservationTimeResponse.from(reservationTime), alreadyBooked);
    }
}
