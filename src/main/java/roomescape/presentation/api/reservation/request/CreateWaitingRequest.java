package roomescape.presentation.api.reservation.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.application.reservation.dto.CreateWaitingParam;

public record CreateWaitingRequest(
        @NotNull
        LocalDate date,
        @NotNull
        Long theme,
        @NotNull
        Long time
) {
    public CreateWaitingParam toServiceParam(Long memberId) {
        return new CreateWaitingParam(date, memberId, theme, time);
    }
}
