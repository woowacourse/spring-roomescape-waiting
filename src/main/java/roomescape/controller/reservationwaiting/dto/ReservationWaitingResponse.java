package roomescape.controller.reservationwaiting.dto;

import java.time.LocalTime;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.domain.reservationwaiting.ReservationWaiting;

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
