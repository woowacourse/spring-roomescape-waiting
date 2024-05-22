package roomescape.web.controller.response;

import java.time.LocalDate;
import roomescape.service.response.ReservationAppResponse;
import roomescape.service.response.ReservationWaitingAppResponse;

public record ReservationMineWebResponse(Long reservationId, ThemeWebResponse theme, LocalDate date,
                                         ReservationTimeWebResponse time, String status) {


    public static ReservationMineWebResponse from(ReservationAppResponse response) {
        return new ReservationMineWebResponse(
                response.id(),
                ThemeWebResponse.from(response.themeAppResponse()),
                response.date().getDate(),
                ReservationTimeWebResponse.from(response.reservationTimeAppResponse()),
                "예약");
    }

    public static ReservationMineWebResponse from(ReservationWaitingAppResponse response) {
        return new ReservationMineWebResponse(
                response.id(),
                ThemeWebResponse.from(response.themeAppResponse()),
                response.date().getDate(),
                ReservationTimeWebResponse.from(response.reservationTimeAppResponse()),
                "예약대기");
    }
}
