package roomescape.reservation.application.dto;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record ReservationChangeCommand(
        String username,
        Long timeId,
        Long themeId,
        LocalDate date
) {
}
