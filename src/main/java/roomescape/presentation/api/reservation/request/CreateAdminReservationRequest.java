package roomescape.presentation.api.reservation.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.application.reservation.command.dto.CreateReservationCommand;

public record CreateAdminReservationRequest(
        @NotNull(message = "date는 필수입니다.")
        LocalDate date,
        @NotNull(message = "timeId는 필수입니다.")
        Long timeId,
        @NotNull(message = "themeId는 필수입니다.")
        Long themeId,
        @NotNull(message = "memberId는 필수입니다.")
        Long memberId
) {

    public CreateReservationCommand toCreateCommand() {
        return new CreateReservationCommand(date, timeId, themeId, memberId);
    }
}
