package roomescape.waiting.dto;

import roomescape.member.dto.MemberResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.waiting.domain.Waiting;

public record WaitingResponse(
        Long id,
        ReservationResponse reservation,
        MemberResponse waitingMember) {
    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                ReservationResponse.from(waiting.getReservation()),
                MemberResponse.from(waiting.getMember()));
    }
}
