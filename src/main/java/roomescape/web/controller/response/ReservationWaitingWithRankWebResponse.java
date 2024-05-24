package roomescape.web.controller.response;

import roomescape.service.response.ReservationWaitingWithRankAppResponse;

import java.time.LocalDate;

public record ReservationWaitingWithRankWebResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeWebResponse time,
        ThemeWebResponse theme,
        Long order,
        String status) {

    public ReservationWaitingWithRankWebResponse(ReservationWaitingWithRankAppResponse response) {
        this(
                response.id(),
                response.name(),
                response.date().getDate(),
                ReservationTimeWebResponse.from(response.reservationTimeAppResponse()),
                ThemeWebResponse.from(response.themeAppResponse()),
                response.rank(),
                response.status()
        );
    }
}
