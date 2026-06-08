package roomescape.service;

import java.time.LocalDate;

public record WaitingSlot(
        LocalDate date,
        Long timeId,
        Long themeId
) {
}
