package roomescape.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AdminReservationCreateRequest(
        @NotNull Long memberId,
        @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId
) {
}
