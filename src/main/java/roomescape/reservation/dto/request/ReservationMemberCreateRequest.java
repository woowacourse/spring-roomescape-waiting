package roomescape.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationMemberCreateRequest(
        @NotNull LocalDate date,
        @NotNull Long themeId,
        @NotNull Long timeId
) {

}
