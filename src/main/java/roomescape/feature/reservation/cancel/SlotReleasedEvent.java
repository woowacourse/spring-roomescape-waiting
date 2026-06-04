package roomescape.feature.reservation.cancel;

import java.time.LocalDate;

public record SlotReleasedEvent(
        Long timeId,
        Long themeId,
        LocalDate date
) {
}
