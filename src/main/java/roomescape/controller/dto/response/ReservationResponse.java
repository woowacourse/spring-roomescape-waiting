package roomescape.controller.dto.response;

import java.time.LocalDate;
import roomescape.service.dto.result.ReservationResult;

public record ReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse timeResponse,
        ThemeResponse themeResponse
) {

    public static ReservationResponse from(ReservationResult result) {
        return new ReservationResponse(
                result.id(),
                result.name(),
                result.date(),
                ReservationTimeResponse.from(result.timeResult()),
                ThemeResponse.from(result.themeResult())
        );
    }
}
