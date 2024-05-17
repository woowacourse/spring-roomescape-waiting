package roomescape.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
}
