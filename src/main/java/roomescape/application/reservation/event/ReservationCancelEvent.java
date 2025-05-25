package roomescape.application.reservation.event;

import java.time.LocalDate;

public record ReservationCancelEvent(
        LocalDate reservationDate,
        Long reservationTimeId,
        Long themeId
) {
}
