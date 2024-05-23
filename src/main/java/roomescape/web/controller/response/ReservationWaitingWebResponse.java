package roomescape.web.controller.response;

import roomescape.domain.ReservationDate;
import roomescape.service.response.ReservationTimeAppResponse;
import roomescape.service.response.ReservationWaitingAppResponse;
import roomescape.service.response.ThemeAppResponse;

public record ReservationWaitingWebResponse(
        Long id,
        String name,
        ReservationDate date,
        ReservationTimeAppResponse reservationTimeAppResponse,
        ThemeAppResponse themeAppResponse
) {

    public static ReservationWaitingWebResponse from(ReservationWaitingAppResponse appResponse) {
        return new ReservationWaitingWebResponse(
                appResponse.id(),
                appResponse.name(),
                appResponse.date(),
                appResponse.reservationTimeAppResponse(),
                appResponse.themeAppResponse());
    }
}
