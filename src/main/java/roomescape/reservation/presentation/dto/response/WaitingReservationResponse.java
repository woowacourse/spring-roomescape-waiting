package roomescape.reservation.presentation.dto.response;

import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingStatus;

public record WaitingReservationResponse(
        Long reservationId,
        Long waitingId,
        WaitingStatus waitingStatus
) {
    public static WaitingReservationResponse from(Waiting waiting) {
        return new WaitingReservationResponse(waiting.getReservation().getId(), waiting.getId(),
                waiting.getWaitingStatus());
    }
}
