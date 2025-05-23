package roomescape.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationStatusServiceResponse(
        long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
}
