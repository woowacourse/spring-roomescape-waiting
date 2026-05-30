package roomescape.reservation.service.dto;

import java.time.LocalDate;

public record ReservationUpdateCommand(
        LocalDate date,
        Long timeId
) {
}
