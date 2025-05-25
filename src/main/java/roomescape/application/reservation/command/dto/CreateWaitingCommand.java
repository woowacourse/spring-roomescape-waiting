package roomescape.application.reservation.command.dto;

import java.time.LocalDate;

public record CreateWaitingCommand(
        LocalDate reservationDate,
        Long themeId,
        Long timeId,
        Long memberId
) {
}
