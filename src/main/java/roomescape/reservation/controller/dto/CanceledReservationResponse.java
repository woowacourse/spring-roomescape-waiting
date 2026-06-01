package roomescape.reservation.controller.dto;

import roomescape.reservation.service.dto.ReservationWaitingResult;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.theme.controller.dto.ThemeResponse;

public record CanceledReservationResponse(
        Long id,
        String guestName,
        String date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status
) implements ReservationWaitingResponse {
    public static CanceledReservationResponse from(ReservationWaitingResult reservationWaitingResult) {
        return new CanceledReservationResponse(
                reservationWaitingResult.id(),
                reservationWaitingResult.guestName(),
                reservationWaitingResult.date().toString(),
                ReservationTimeResponse.from(reservationWaitingResult.time()),
                ThemeResponse.from(reservationWaitingResult.theme()),
                reservationWaitingResult.status().toString()
        );
    }
}
