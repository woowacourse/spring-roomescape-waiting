package roomescape.web.controller.response;

import java.time.LocalDate;
import roomescape.service.response.ReservationAppResponse;
import roomescape.service.response.ReservationWaitingAppResponseWithRank;

public record ReservationMineWebResponse(
        Long id,
        ThemeWebResponse theme,
        LocalDate date,
        ReservationTimeWebResponse time,
        String status) {


    public static ReservationMineWebResponse from(ReservationAppResponse response) {
        return new ReservationMineWebResponse(
                response.id(),
                ThemeWebResponse.from(response.themeAppResponse()),
                response.date().getDate(),
                ReservationTimeWebResponse.from(response.reservationTimeAppResponse()),
                "예약");
    }

    public static ReservationMineWebResponse from(ReservationWaitingAppResponseWithRank response) {
        return new ReservationMineWebResponse(
                response.id(),
                ThemeWebResponse.from(response.themeAppResponse()),
                response.date().getDate(),
                ReservationTimeWebResponse.from(response.reservationTimeAppResponse()),
                response.rank() + "번째 예약대기");
    }
}
