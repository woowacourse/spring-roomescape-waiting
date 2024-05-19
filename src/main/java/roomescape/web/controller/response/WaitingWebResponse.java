package roomescape.web.controller.response;

import roomescape.service.response.WaitingAppResponse;

import java.time.LocalDate;

public record WaitingWebResponse(
    Long id,
    String name,
    LocalDate date,
    ReservationTimeWebResponse time,
    ThemeWebResponse theme) {

    public WaitingWebResponse(WaitingAppResponse response) {
        this(
            response.id(),
            response.name(),
            response.date().getDate(),
            ReservationTimeWebResponse.from(response.reservationTimeAppResponse()),
            ThemeWebResponse.from(response.themeAppResponse())
        );
    }
}
