package roomescape.reservation.event.schema;

import java.time.LocalDate;

public record ReservationCancelRequested(
        Long reservationId,
        LocalDate date,
        Long themeId,
        Long timeId
) {
}
