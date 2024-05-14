package roomescape.controller.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationResponse(
        Long id,
        String themeName,
        LocalDate date,
        LocalTime time,
        String status) {
}
