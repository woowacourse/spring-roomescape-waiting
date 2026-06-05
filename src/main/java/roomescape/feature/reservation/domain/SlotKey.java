package roomescape.feature.reservation.domain;

import java.time.LocalDate;

public record SlotKey(
        LocalDate date,
        Long timeId,
        Long themeId
) {
}
