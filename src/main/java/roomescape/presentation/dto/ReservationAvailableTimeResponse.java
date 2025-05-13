package roomescape.presentation.dto;

import roomescape.business.domain.PlayTime;

public record ReservationAvailableTimeResponse(PlayTime playTime, boolean alreadyBooked) {
}
