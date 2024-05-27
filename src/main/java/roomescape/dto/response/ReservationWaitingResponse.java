package roomescape.dto.response;

import roomescape.domain.ReservationWaiting;

public record ReservationWaitingResponse(Long id, Long memberId, Long reservationId, Integer rank) {

    public static ReservationWaitingResponse of(ReservationWaiting reservationWaiting, Integer rank) {
        return new ReservationWaitingResponse(
                reservationWaiting.getId(),
                reservationWaiting.getMember().getId(),
                reservationWaiting.getReservation().getId(),
                rank
        );
    }
}
