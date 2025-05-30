package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.domain.ReservationWaiting;

public record ReservationWaitingResponse(
        long id,
        LocalDate date,
        ReservationTimeResponse time,
        ReservationThemeResponse theme
) {
    public static ReservationWaitingResponse from(final ReservationWaiting reservationWaiting) {
        return new ReservationWaitingResponse(
                reservationWaiting.getId(),
                reservationWaiting.getDate(),
                ReservationTimeResponse.from(reservationWaiting.getTime()),
                ReservationThemeResponse.from(reservationWaiting.getTheme())
        );
    }
}
