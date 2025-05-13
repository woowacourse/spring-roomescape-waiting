package roomescape.reservation.presentation.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MemberReservationRequest(
        @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId
) {
}
