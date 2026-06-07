package roomescape.reservation.controller.dto;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationEntry;
import roomescape.reservation.domain.Slot;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.theme.controller.dto.ThemeResponse;

public record ReservationResponse(
        long id,
        String status,
        Long waitingRank,
        String name,
        String date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public static ReservationResponse from(ReservationEntry reservationEntry) {
        Reservation reservation = reservationEntry.reservation();
        Slot slot = reservation.getSlot();

        return new ReservationResponse(
                reservation.getId(),
                reservationEntry.status().name(),
                reservationEntry.waitingRank(),
                reservation.getName(),
                slot.date().toString(),
                ReservationTimeResponse.from(slot.time()),
                ThemeResponse.from(slot.theme())
        );
    }
}
