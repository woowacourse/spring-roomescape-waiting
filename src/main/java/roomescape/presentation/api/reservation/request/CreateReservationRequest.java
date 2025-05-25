package roomescape.presentation.api.reservation.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.application.reservation.command.dto.CreateReservationCommand;

public record CreateReservationRequest(
        @NotNull(message = "date는 필수입니다.")
        LocalDate date,
        @NotNull(message = "timeId는 필수입니다.")
        Long timeId,
        @NotNull(message = "themeId는 필수입니다.")
        Long themeId
) {

    public CreateReservationCommand toCreateCommand(Long memberId) {
        return new CreateReservationCommand(date, timeId, themeId, memberId);
    }
}
