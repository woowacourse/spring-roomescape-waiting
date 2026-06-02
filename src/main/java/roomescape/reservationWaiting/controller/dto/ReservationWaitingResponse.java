package roomescape.reservationWaiting.controller.dto;

import java.time.LocalDate;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.theme.controller.dto.ThemeResponse;
import roomescape.time.controller.dto.ReservationTimeResponse;

public record ReservationWaitingResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public static ReservationWaitingResponse from(ReservationWaiting reservationWaiting) {
        return new ReservationWaitingResponse(
                reservationWaiting.getId(),
                reservationWaiting.getName(),
                reservationWaiting.getDate(),
                ReservationTimeResponse.from(reservationWaiting.getTime()),
                ThemeResponse.from(reservationWaiting.getTheme())
        );
    }
}
