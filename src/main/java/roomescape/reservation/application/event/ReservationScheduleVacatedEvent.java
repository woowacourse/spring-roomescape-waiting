package roomescape.reservation.application.event;

import java.time.LocalDate;

public record ReservationScheduleVacatedEvent(
        LocalDate date,
        Long themeId,
        Long timeId
) {
}
