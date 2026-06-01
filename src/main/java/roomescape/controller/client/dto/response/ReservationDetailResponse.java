package roomescape.controller.client.dto.response;

import java.time.LocalDate;
import roomescape.service.result.ReservationResult;

public record ReservationDetailResponse(
        long reservationId,
        LocalDate date,
        ThemeResponse theme,
        ReservationTimeResponse time,
        ReservationEntryResponse entry
) {

    public static ReservationDetailResponse from(ReservationResult result) {
        return new ReservationDetailResponse(
                result.reservationId(),
                result.date(),
                ThemeResponse.from(result.theme()),
                ReservationTimeResponse.from(result.time()),
                ReservationEntryResponse.from(result.entry())
        );
    }
}
