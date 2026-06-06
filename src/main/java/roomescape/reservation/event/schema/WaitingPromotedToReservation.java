package roomescape.reservation.event.schema;

import java.time.LocalDate;

public record WaitingPromotedToReservation(
        LocalDate date,
        Long themeId,
        Long timeId
) {
}
