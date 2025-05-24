package roomescape.reservation.ui.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateBookedReservationRequest(
        @NotNull
        Long memberId,
        @NotNull
        LocalDate date,
        @NotNull
        Long timeId,
        @NotNull
        Long themeId
) {

    public record ForMember(
            @NotNull
            LocalDate date,
            @NotNull
            Long timeId,
            @NotNull
            Long themeId
    ) {

    }
}
