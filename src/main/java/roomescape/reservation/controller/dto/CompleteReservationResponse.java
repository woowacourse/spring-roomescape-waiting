package roomescape.reservation.controller.dto;

import roomescape.reservation.repository.dto.ReservationWaitingResult;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.theme.controller.dto.ThemeResponse;

public record CompleteReservationResponse(
        Long id,
        String guestName,
        String date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status,
        boolean isConfirmed
) implements ReservationWaitingResponse {

    public static CompleteReservationResponse from(ReservationWaitingResult reservationWaitingResult) {
        return new CompleteReservationResponse(
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
