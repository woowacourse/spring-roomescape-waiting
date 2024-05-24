package roomescape.web.controller.response;

import roomescape.service.response.ReservationWaitingAppResponse;

import java.time.LocalDate;

public record ReservationWaitingResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status) {

    public ReservationWaitingResponse(ReservationWaitingAppResponse response) {
        this(
                response.id(),
                response.name(),
                response.date().getDate(),
                ReservationTimeResponse.from(response.reservationTimeAppResponse()),
                ThemeResponse.from(response.themeAppResponse()),
                response.status()
        );
    }
}
