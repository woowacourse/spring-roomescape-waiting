package roomescape.web.controller.response;

import java.time.LocalDate;

import roomescape.service.response.ReservationAppResponse;

public record MemberReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme) {

    public static MemberReservationResponse from(ReservationAppResponse appResponse) {
        return new MemberReservationResponse(
                appResponse.id(),
                appResponse.name(),
                appResponse.date().getDate(),
                ReservationTimeResponse.from(appResponse.reservationTimeAppResponse()),
                ThemeResponse.from(appResponse.themeAppResponse())
        );
    }
}
