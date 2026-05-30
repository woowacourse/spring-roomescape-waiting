package roomescape.reservationWaiting.service.dto;

import java.time.LocalDate;

public record ReservationWaitingCommand(
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {
}
