package roomescape.dto.response;

import roomescape.domain.ReservationStatus;

public record WaitingResponse(
        ReservationStatus reservationStatus,
        Long waitingRank) {
}
