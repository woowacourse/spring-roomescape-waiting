package roomescape.application.reservation.command.dto;

import java.time.LocalDate;

public record CreateReservationCommand(
        LocalDate date,
        Long timeId,
        Long themeId,
        Long memberId
) {
}
