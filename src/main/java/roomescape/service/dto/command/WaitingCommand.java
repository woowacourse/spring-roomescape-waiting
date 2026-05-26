package roomescape.service.dto.command;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WaitingCommand(
        String name,
        LocalDate date,
        Long timeId,
        Long themeId,
        LocalDateTime createAt
) {
}
