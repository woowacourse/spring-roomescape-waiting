package roomescape.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.Time;

public record ReservationRequest(
        @NotNull LocalDate date,
        Long timeId,
        Long themeId
) {
}
