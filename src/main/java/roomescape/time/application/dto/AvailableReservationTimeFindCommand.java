package roomescape.time.application.dto;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record AvailableReservationTimeFindCommand(
        Long themeId,
        LocalDate date
) {
}
