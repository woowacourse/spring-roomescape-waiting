package roomescape.web.controller.response;

import roomescape.service.response.ReservationWaitingAppResponse;

import java.time.LocalDate;

public record ReservationWaitingWebResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeWebResponse time,
        ThemeWebResponse theme,
        boolean isDenied) {

    public ReservationWaitingWebResponse(ReservationWaitingAppResponse response) {
        this(
                response.id(),
                response.name(),
                response.date().getDate(),
                ReservationTimeWebResponse.from(response.reservationTimeAppResponse()),
                ThemeWebResponse.from(response.themeAppResponse()),
                response.isDenied()
        );
    }
}
