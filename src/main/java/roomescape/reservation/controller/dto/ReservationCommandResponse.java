package roomescape.reservation.controller.dto;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Slot;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.theme.controller.dto.ThemeResponse;

public record ReservationCommandResponse(
        long id,
        String name,
        String date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public static ReservationCommandResponse from(Reservation reservation) {
        Slot slot = reservation.getSlot();

        return new ReservationCommandResponse(
                reservation.getId(),
                reservation.getName(),
                slot.date().toString(),
                ReservationTimeResponse.from(slot.time()),
                ThemeResponse.from(slot.theme())
        );
    }
}
