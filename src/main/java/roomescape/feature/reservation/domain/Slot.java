package roomescape.feature.reservation.domain;

import java.time.LocalDate;

public record Slot(
        Long timeId,
        Long themeId,
        LocalDate date
) {
}
