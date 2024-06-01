package roomescape.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationAdminCreateRequest(
        @NotNull LocalDate date,
        @NotNull Long themeId,
        @NotNull Long timeId,
        @NotNull Long memberId
) {

}
