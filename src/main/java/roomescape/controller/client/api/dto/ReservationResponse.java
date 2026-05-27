package roomescape.controller.client.api.dto;

import java.time.LocalDate;
import roomescape.service.result.ReservationResult;

public record ReservationResponse(
        long reservationId,
        LocalDate date,
        ThemeResponse theme,
        ReservationTimeResponse time,
        ReservationEntryResponse entry
) {

    public static ReservationResponse from(ReservationResult result) {
        return new ReservationResponse(
                result.reservationId(),
                result.date(),
                ThemeResponse.from(result.theme()),
                ReservationTimeResponse.from(result.time()),
                ReservationEntryResponse.from(result.entry())
        );
    }
}
