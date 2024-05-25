package roomescape.web.controller.response;

import roomescape.service.response.ReservationWaitingWithRankDto;

import java.time.LocalDate;

public record ReservationWaitingWithRankResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        Long order,
        String status) {

    public ReservationWaitingWithRankResponse(ReservationWaitingWithRankDto response) {
        this(
                response.id(),
                response.name(),
                response.date().getDate(),
                ReservationTimeResponse.from(response.reservationTimeDto()),
                ThemeResponse.from(response.themeDto()),
                response.rank(),
                response.status()
        );
    }
}
