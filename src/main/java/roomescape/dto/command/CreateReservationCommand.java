package roomescape.dto.command;

import java.time.LocalDate;

public record CreateReservationCommand(
        long memberId,
        LocalDate date,
        long timeId,
        long themeId
) {
}
