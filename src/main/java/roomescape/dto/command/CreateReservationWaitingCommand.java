package roomescape.dto.command;

import java.time.LocalDate;

public record CreateReservationWaitingCommand(
        String name,
        LocalDate reservationDate,
        long timeId,
        long themeId
) {
}
