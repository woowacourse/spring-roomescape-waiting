package roomescape.dto.theme;

import java.util.List;

public record ThemeReservationTimeResponses(
        List<ThemeReservationTimeResponse> times
) {
    public static ThemeReservationTimeResponses from(List<ThemeReservationTimeResponse> times) {
        return new ThemeReservationTimeResponses(times);
    }
}
