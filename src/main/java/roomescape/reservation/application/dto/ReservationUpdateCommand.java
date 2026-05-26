package roomescape.reservation.application.dto;

import java.time.LocalDate;

public record ReservationUpdateCommand(
        Long id,
        String name,
        LocalDate date,
        Long timeId
) {
}
