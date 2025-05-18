package roomescape.presentation.dto;

import roomescape.business.domain.ReservationTime;

public record ReservationAvailableTimeResponse(ReservationTimeResponse reservationTimeResponse, boolean alreadyBooked) {

    public static ReservationAvailableTimeResponse from(ReservationTime reservationTime, boolean alreadyBooked) {
        return new ReservationAvailableTimeResponse(ReservationTimeResponse.from(reservationTime), alreadyBooked);
    }
}
