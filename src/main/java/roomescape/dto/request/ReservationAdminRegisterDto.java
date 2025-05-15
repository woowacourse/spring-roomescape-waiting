package roomescape.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationAdminRegisterDto(
        @NotNull LocalDate date,
        @NotNull Long themeId,
        @NotNull Long timeId,
        @NotNull Long memberId
) {
}
