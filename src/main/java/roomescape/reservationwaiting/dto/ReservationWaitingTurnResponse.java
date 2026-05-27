package roomescape.reservationwaiting.dto;

import roomescape.reservationwaiting.domain.ReservationWaiting;

public record ReservationWaitingTurnResponse(Long id, String name, Long reservationId, Long turn) {

    public static ReservationWaitingTurnResponse from(ReservationWaiting reservationWaiting, Long turn) {
        return new ReservationWaitingTurnResponse(reservationWaiting.getId(), reservationWaiting.getName(),
                reservationWaiting.getReservation().getId(), turn);
    }
}
