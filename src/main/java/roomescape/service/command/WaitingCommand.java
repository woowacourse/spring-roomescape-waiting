package roomescape.service.command;

import java.time.LocalDate;

public record WaitingCommand(
        String name,
        LocalDate date,
        long timeId,
        long themeId
) {
}
