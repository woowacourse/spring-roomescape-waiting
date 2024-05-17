package roomescape.web.controller.response;

import java.time.LocalDate;
import roomescape.service.response.ReservationAppResponse;

public record MemberReservationWebResponse(
    Long id,
    String name,
    LocalDate date,
    ReservationTimeWebResponse time,
    ThemeWebResponse theme) {

    public static MemberReservationWebResponse from(ReservationAppResponse appResponse) {
        return new MemberReservationWebResponse(
            appResponse.id(),
            appResponse.name(),
            appResponse.date().getDate(),
            ReservationTimeWebResponse.from(appResponse.reservationTimeAppResponse()),
            ThemeWebResponse.from(appResponse.themeAppResponse())
        );
    }
}
