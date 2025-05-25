package roomescape.reservation.controller.response;

import roomescape.member.controller.response.MemberResponse;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.Waiting;

public record WaitingResponse(

        Long id,
        ReservationResponse reservationResponse,
        MemberResponse memberResponse,
        ReservationStatus reservationStatus
) {

    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                ReservationResponse.from(waiting.getReservation()),
                MemberResponse.from(waiting.getMember()),
                ReservationStatus.WAITING
        );
    }
}
