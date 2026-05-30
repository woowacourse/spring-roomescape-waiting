package roomescape.reservationWaiting.controller.dto;

import java.time.LocalDate;
import roomescape.reservationWaiting.service.dto.ReservationWaitingResult;
import roomescape.theme.controller.dto.ThemeResponse;
import roomescape.time.controller.dto.ReservationTimeResponse;

public record ReservationWaitingResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public static ReservationWaitingResponse from(ReservationWaitingResult result) {
        return new ReservationWaitingResponse(
                result.id(),
                result.name(),
                result.date(),
                ReservationTimeResponse.from(result.time()),
                ThemeResponse.from(result.theme())
        );
    }
}
