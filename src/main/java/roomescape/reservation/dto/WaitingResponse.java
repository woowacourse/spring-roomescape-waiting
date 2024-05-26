package roomescape.reservation.dto;

import roomescape.reservation.model.Waiting;

public record WaitingResponse(Long reservationId, Long memberId) {
    public static WaitingResponse from(Waiting savedWaiting) {
        return new WaitingResponse(
                savedWaiting.getReservation().getId(),
                savedWaiting.getMember().getId()
        );
    }
}
