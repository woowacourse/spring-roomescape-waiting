package roomescape.reservation.ui.waiting.dto;

import roomescape.member.ui.dto.MemberResponse;
import roomescape.reservation.application.waiting.dto.ReservationWaitingInfo;
import roomescape.reservation.ui.reservation.dto.ReservationResponse;

public record ReservationWaitingResponse(
        long id,
        ReservationResponse reservation,
        MemberResponse member
) {

    public ReservationWaitingResponse(final ReservationWaitingInfo reservationWaitingInfo) {
        this(reservationWaitingInfo.id(),
                new ReservationResponse(reservationWaitingInfo.reservation()),
                new MemberResponse(reservationWaitingInfo.member())
        );
    }
}
