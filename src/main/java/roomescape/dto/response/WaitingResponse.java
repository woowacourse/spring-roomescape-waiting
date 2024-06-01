package roomescape.dto.response;

import static roomescape.domain.Reservation.Status;

public record WaitingResponse(
        Status reservationStatus,
        Long waitingRank) {
}
