package roomescape.reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservation(
        Long id,
        String name,
        String themeName,
        LocalDate date,
        LocalTime startAt,
        String resourceType,
        String status,
        Long waitingNumber
) {
}
