package roomescape.reservation.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationUpdateCommand(
        LocalDate date,
        Long timeId,
        LocalDateTime now
) {
}
