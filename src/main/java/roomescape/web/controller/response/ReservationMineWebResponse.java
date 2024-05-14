package roomescape.web.controller.response;

import roomescape.service.response.ReservationAppResponse;

import java.time.LocalDate;

public record ReservationMineWebResponse(Long reservationId, ThemeWebResponse theme, LocalDate date,
                                         ReservationTimeWebResponse time, String status) {

    public ReservationMineWebResponse(ReservationAppResponse reservation) {
        this(reservation.id(),
                ThemeWebResponse.from(reservation.themeAppResponse()),
                reservation.date().getDate(),
                ReservationTimeWebResponse.from(reservation.reservationTimeAppResponse()),
                reservation.reservationStatus());
    }
}
