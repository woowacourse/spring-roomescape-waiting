package roomescape.reservation.service.dto.response;

import roomescape.theme.service.dto.response.ThemeResponse;

import java.time.LocalDate;
import java.util.List;

public record ReservationOptionResponse(
        List<LocalDate> dates,
        List<ThemeResponse> themes
) {
}
