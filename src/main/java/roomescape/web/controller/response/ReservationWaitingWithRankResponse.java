package roomescape.web.controller.response;

import roomescape.service.response.ReservationWaitingWithRankAppResponse;

import java.time.LocalDate;

public record ReservationWaitingWithRankResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        Long order,
        String status) {

    public ReservationWaitingWithRankResponse(ReservationWaitingWithRankAppResponse response) {
        this(
                response.id(),
                response.name(),
                response.date().getDate(),
                ReservationTimeResponse.from(response.reservationTimeAppResponse()),
                ThemeResponse.from(response.themeAppResponse()),
                response.rank(),
                response.status()
        );
    }
}
