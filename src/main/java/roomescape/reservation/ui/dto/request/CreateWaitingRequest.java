package roomescape.reservation.ui.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateWaitingRequest(
        @NotNull
        LocalDate date,
        @NotNull
        Long timeId,
        @NotNull
        Long themeId,
        @NotNull
        Long memberId
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
