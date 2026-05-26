package roomescape.reservation.controller.dto;

import roomescape.reservation.service.dto.ReservationWaitingResult;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.theme.controller.dto.ThemeResponse;

public record ReservationWaitingResponse(
        Long id,
        String guestName,
        String date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status,
        long waitNumber
) {

    public static ReservationWaitingResponse from(ReservationWaitingResult reservationWaitingResult) {
        return new ReservationWaitingResponse(
                reservationWaitingResult.id(),
                reservationWaitingResult.guestName(),
                reservationWaitingResult.date().toString(),
                ReservationTimeResponse.from(reservationWaitingResult.time()),
                ThemeResponse.from(reservationWaitingResult.theme()),
                reservationWaitingResult.status().toString(),
                reservationWaitingResult.waitNumber()
        );
    }
}
