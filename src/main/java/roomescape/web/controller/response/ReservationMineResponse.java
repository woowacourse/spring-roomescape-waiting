package roomescape.web.controller.response;

import roomescape.service.response.ReservationDto;

import java.time.LocalDate;

public record ReservationMineResponse(Long reservationId, ThemeResponse theme, LocalDate date,
                                      ReservationTimeResponse time) {

    public ReservationMineResponse(ReservationDto reservation) {
        this(reservation.id(),
                ThemeResponse.from(reservation.themeDto()),
                reservation.date().getDate(),
                ReservationTimeResponse.from(reservation.reservationTimeDto()));
    }
}
