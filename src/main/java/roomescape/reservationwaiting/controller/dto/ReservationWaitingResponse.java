package roomescape.reservationwaiting.controller.dto;

import java.time.LocalTime;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservationwaiting.ReservationWaiting;

public record ReservationWaitingResponse(
        Long id,
        String name,
        LocalTime requestAt,
        ReservationResponse reservationResponse
) {
    public static ReservationWaitingResponse from(final ReservationWaiting reservationWaiting) {
        return new ReservationWaitingResponse(
                reservationWaiting.getId(),
                reservationWaiting.getName(),
                reservationWaiting.getRequestAt(),
                ReservationResponse.from(reservationWaiting.getReservation())
        );
    }
}
