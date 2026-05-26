package roomescape.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AdminReservationRequestDto(
        @NotNull Long memberId,
        @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId,
        Long storeId
) {
}
