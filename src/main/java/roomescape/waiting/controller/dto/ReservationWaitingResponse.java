package roomescape.waiting.controller.dto;

import java.time.LocalDate;
import roomescape.theme.controller.dto.ThemeResponse;
import roomescape.time.controller.dto.ReservationTimeResponse;
import roomescape.waiting.service.dto.ReservationWaitingResult;

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
