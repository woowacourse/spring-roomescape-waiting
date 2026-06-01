package roomescape.controller.reservationwaiting.dto;

import java.time.LocalDateTime;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.domain.reservationwaiting.ReservationWaiting;

public record ReservationWaitingResponse(
        Long id,
        String name,
        LocalDateTime requestedAt,
        ReservationResponse reservationResponse
) {
    public static ReservationWaitingResponse from(final ReservationWaiting reservationWaiting) {
        return new ReservationWaitingResponse(
                reservationWaiting.getId(),
                reservationWaiting.getName(),
                reservationWaiting.getRequestedAt(),
                ReservationResponse.from(reservationWaiting.getReservation())
        );
    }
}
