package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationWithStatusResponse(
        Long reservationId,
        String themeName,
        LocalDate date,
        LocalTime time,
        String status
) {
}
