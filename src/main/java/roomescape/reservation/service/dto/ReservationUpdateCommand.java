package roomescape.reservation.service.dto;

import java.time.LocalDate;

public record ReservationUpdateCommand(
        Long id,
        String name,
        LocalDate date,
        Long timeId
) {
}
