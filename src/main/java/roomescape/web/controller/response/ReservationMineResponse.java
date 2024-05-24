package roomescape.web.controller.response;

import roomescape.service.response.ReservationAppResponse;

import java.time.LocalDate;

public record ReservationMineResponse(Long reservationId, ThemeResponse theme, LocalDate date,
                                      ReservationTimeResponse time) {

    public ReservationMineResponse(ReservationAppResponse reservation) {
        this(reservation.id(),
                ThemeResponse.from(reservation.themeAppResponse()),
                reservation.date().getDate(),
                ReservationTimeResponse.from(reservation.reservationTimeAppResponse()));
    }
}
