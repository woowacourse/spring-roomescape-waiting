package roomescape.controller.client.api.dto.response;

import java.time.LocalDate;

public record ReservationSlotDetailResponse(
        long slotId,
        LocalDate date,
        ThemeResponse theme,
        ReservationTimeResponse time,
        ReservationResponse reservation
) {
}
