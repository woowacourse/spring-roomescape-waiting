package roomescape.reservation.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AdminReservationRequestDto(
        @NotNull Long memberId,
        @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId,
        @NotNull Long storeId
) {
}
