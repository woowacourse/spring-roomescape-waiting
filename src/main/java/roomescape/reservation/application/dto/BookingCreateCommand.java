package roomescape.reservation.application.dto;

import java.time.LocalDate;

public record BookingCreateCommand(
        String name,
        LocalDate date,
        Long themeId,
        Long timeId
) {
}
