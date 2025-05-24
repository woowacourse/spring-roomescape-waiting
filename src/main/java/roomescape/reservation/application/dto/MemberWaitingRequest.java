package roomescape.reservation.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MemberWaitingRequest(
        @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId
) {
}
