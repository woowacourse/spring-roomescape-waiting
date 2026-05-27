package roomescape.reservationwaiting.dto;

import roomescape.reservationwaiting.domain.ReservationWaiting;

public record ReservationWaitingResponse(Long id, String name, Long reservationId) {

    public static ReservationWaitingResponse from(ReservationWaiting reservationWaiting) {
        return new ReservationWaitingResponse(reservationWaiting.getId(), reservationWaiting.getName(),
                reservationWaiting.getReservation().getId());
    }
}
