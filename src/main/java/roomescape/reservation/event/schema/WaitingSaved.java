package roomescape.reservation.event.schema;

import java.time.LocalDate;

public record WaitingSaved(
        LocalDate date,
        Long themeId,
        Long timeId
) {
}
