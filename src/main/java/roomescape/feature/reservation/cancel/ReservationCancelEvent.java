package roomescape.feature.reservation.cancel;

import java.time.LocalDate;

public record ReservationCancelEvent(
        Long timeId,
        Long themeId,
        LocalDate date
) {
}
