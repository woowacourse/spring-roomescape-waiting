package roomescape.controller.client.api.dto.response;

import java.time.LocalDate;

public record ReservationDetailResponse(
        long slotId,
        LocalDate date,
        ThemeResponse theme,
        ReservationTimeResponse time,
        ReservationResponse reservation
) {
}
