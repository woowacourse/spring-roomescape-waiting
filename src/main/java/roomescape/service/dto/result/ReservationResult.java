package roomescape.service.dto.result;

import java.time.LocalDate;

public record ReservationResult(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResult time,
        ThemeResult theme
) {
}
