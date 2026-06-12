package roomescape.reservation.controller.dto.response;

import roomescape.theme.controller.dto.response.ThemeResponse;

import java.time.LocalDate;
import java.util.List;

public record ReservationOptionResponse(
        List<LocalDate> dates,
        List<ThemeResponse> themes
) {
}
