package roomescape.dto;

import java.time.LocalDate;

public record WaitingListCreateCommand(
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {
}
