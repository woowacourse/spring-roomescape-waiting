package roomescape.service.response;

import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationWaiting;

public record ReservationWaitingAppResponse(
        Long id,
        String name,
        ReservationDate date,
        ReservationTimeAppResponse reservationTimeAppResponse,
        ThemeAppResponse themeAppResponse,
        long priority) {

    public static ReservationWaitingAppResponse from(ReservationWaiting waiting) {
        return new ReservationWaitingAppResponse(
                waiting.getId(),
                waiting.getMember().getName().getName(),
                waiting.getReservation().getReservationDate(),
                ReservationTimeAppResponse.from(waiting.getReservation().getReservationTime()),
                ThemeAppResponse.from(waiting.getReservation().getTheme()),
                waiting.getPriority()
        );
    }
}
