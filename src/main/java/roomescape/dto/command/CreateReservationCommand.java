package roomescape.dto.command;

import java.time.LocalDate;

public record CreateReservationCommand(
        String name,
        LocalDate date,
        long timeId,
        long themeId
) {
}
