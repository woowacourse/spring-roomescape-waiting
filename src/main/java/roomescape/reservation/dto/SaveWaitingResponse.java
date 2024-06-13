package roomescape.reservation.dto;

import roomescape.reservation.model.Waiting;

public record SaveWaitingResponse(Long reservationId, Long memberId) {
    public static SaveWaitingResponse from(Waiting savedWaiting) {
        return new SaveWaitingResponse(
                savedWaiting.getReservation().getId(),
                savedWaiting.getMember().getId()
        );
    }
}
