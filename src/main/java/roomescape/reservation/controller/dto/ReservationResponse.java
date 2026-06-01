package roomescape.reservation.controller.dto;

import roomescape.reservation.domain.Reservation;
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

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getStatus().name(),
                reservation.getWaitingRank(),
                reservation.getName(),
                reservation.getDate().toString(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}
