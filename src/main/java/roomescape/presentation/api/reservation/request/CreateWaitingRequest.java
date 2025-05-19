package roomescape.presentation.api.reservation.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.application.reservation.command.dto.CreateWaitingCommand;

public record CreateWaitingRequest(
        @NotNull(message = "date는 필수입니다.")
        LocalDate date,
        @NotNull(message = "themeId는 필수입니다.")
        Long theme,
        @NotNull(message = "themeId는 필수입니다.")
        Long time
) {

    public CreateWaitingCommand toCreateCommand(Long memberId) {
        return new CreateWaitingCommand(
                date,
                theme,
                time,
                memberId
        );
    }
}
