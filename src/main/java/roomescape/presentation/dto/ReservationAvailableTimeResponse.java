package roomescape.presentation.dto;

import roomescape.business.domain.ReservationTime;

public record ReservationAvailableTimeResponse(ReservationTime reservationTime, boolean alreadyBooked) {
}
