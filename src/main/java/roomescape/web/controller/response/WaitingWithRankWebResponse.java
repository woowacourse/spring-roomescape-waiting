package roomescape.web.controller.response;

import roomescape.service.response.WaitingWithRankAppResponse;

import java.time.LocalDate;

public record WaitingWithRankWebResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeWebResponse time,
        ThemeWebResponse theme,
        Long order) {

    public WaitingWithRankWebResponse(WaitingWithRankAppResponse response) {
        this(
                response.id(),
                response.name(),
                response.date().getDate(),
                ReservationTimeWebResponse.from(response.reservationTimeAppResponse()),
                ThemeWebResponse.from(response.themeAppResponse()),
                response.rank()
        );
    }
}
