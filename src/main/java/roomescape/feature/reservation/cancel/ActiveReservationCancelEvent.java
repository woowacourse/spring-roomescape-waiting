package roomescape.feature.reservation.cancel;

import java.time.LocalDate;

public record ActiveReservationCancelEvent(
        Long timeId,
        Long themeId,
        LocalDate date
) {
}
