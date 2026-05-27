package roomescape.reservation.controller.dto;

import roomescape.reservation.service.dto.ReservationWaitingResult;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.theme.controller.dto.ThemeResponse;

public record ConfirmedReservationResponse(
        Long id,
        String guestName,
        String date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status,
        boolean isConfirmed
) implements ReservationWaitingResponse {

    public static ConfirmedReservationResponse from(ReservationWaitingResult reservationWaitingResult) {
        return new ConfirmedReservationResponse(
                reservationWaitingResult.id(),
                reservationWaitingResult.guestName(),
                reservationWaitingResult.date().toString(),
                ReservationTimeResponse.from(reservationWaitingResult.time()),
                ThemeResponse.from(reservationWaitingResult.theme()),
                reservationWaitingResult.status().toString(),
                reservationWaitingResult.status().isConfirmed()
        );
    }
}
