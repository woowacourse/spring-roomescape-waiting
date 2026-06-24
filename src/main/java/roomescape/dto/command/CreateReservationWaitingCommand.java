package roomescape.dto.command;

import java.time.LocalDate;

public record CreateReservationWaitingCommand(
        long memberId,
        LocalDate reservationDate,
        long timeId,
        long themeId
) {
}
