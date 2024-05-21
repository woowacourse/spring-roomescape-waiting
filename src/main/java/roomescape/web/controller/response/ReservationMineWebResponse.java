package roomescape.web.controller.response;

import java.time.LocalDate;
import roomescape.domain.ReservationStatus;
import roomescape.service.response.ReservationAppResponse;

public record ReservationMineWebResponse(Long reservationId, ThemeWebResponse theme, LocalDate date,
                                         ReservationTimeWebResponse time, String status) {

    public ReservationMineWebResponse(ReservationAppResponse appResponse) {
        this(
                appResponse.id(),
                ThemeWebResponse.from(appResponse.themeAppResponse()),
                appResponse.date().getDate(),
                ReservationTimeWebResponse.from(appResponse.reservationTimeAppResponse()),
                getStatusData(appResponse.reservationStatus())
        );
    }

    private static String getStatusData(ReservationStatus reservationStatus) {
        if (reservationStatus.getStatus().isReserved()) {
            return "예약";
        }
        return reservationStatus.getPriority() + "번째 예약대기";
    }
}
