package roomescape.presentation.api.reservation.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.application.reservation.command.dto.CreateReservationTimeCommand;

public record CreateReservationTimeRequest(
        @NotNull(message = "startAt는 필수입니다.")
        LocalTime startAt
) {

    public CreateReservationTimeCommand toCreateCommand() {
        return new CreateReservationTimeCommand(startAt);
    }
}
