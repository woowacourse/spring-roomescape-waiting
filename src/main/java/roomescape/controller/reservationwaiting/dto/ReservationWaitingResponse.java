package roomescape.controller.reservationwaiting.dto;

import java.time.LocalDateTime;
import roomescape.controller.reservationslot.dto.ReservationSlotResponse;
import roomescape.domain.reservationwaiting.ReservationWaiting;

public record ReservationWaitingResponse(
        Long id,
        String name,
        LocalDateTime requestedAt,
        ReservationSlotResponse slotResponse
) {
    public static ReservationWaitingResponse from(final ReservationWaiting reservationWaiting) {
        return new ReservationWaitingResponse(
                reservationWaiting.getId(),
                reservationWaiting.getName(),
                reservationWaiting.getRequestedAt(),
                ReservationSlotResponse.from(reservationWaiting.getSlot())
        );
    }
}
