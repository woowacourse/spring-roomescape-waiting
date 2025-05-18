package roomescape.reservation.ui.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MemberCreateReservationRequest(
        @NotNull
        LocalDate date,
        @NotNull
        Long timeId,
        @NotNull
        Long themeId
) {
}
