package roomescape.controller.client.api.dto.response;

import java.time.LocalDate;
import roomescape.application.service.result.ReservationSlotResult;

public record ReservationSlotResponse(
        long slotId,
        LocalDate date,
        ThemeResponse theme,
        ReservationTimeResponse time,
        ReservationResponse reservation
) {

    public static ReservationSlotResponse from(ReservationSlotResult result) {
        return new ReservationSlotResponse(
                result.slotId(),
                result.date(),
                ThemeResponse.from(result.theme()),
                ReservationTimeResponse.from(result.time()),
                ReservationResponse.from(result.reservation())
        );
    }
}
