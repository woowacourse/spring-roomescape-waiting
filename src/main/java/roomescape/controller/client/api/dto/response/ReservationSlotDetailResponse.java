package roomescape.controller.client.api.dto.response;

import java.time.LocalDate;
import roomescape.service.result.ReservationSlotResult;

public record ReservationSlotDetailResponse(
        long slotId,
        LocalDate date,
        ThemeResponse theme,
        ReservationTimeResponse time,
        ReservationResponse reservation
) {

    public static ReservationSlotDetailResponse from(ReservationSlotResult result) {
        return new ReservationSlotDetailResponse(
                result.slotId(),
                result.date(),
                ThemeResponse.from(result.theme()),
                ReservationTimeResponse.from(result.time()),
                ReservationResponse.from(result.reservation())
        );
    }
}
