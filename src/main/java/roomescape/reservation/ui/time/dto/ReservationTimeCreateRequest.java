package roomescape.reservation.ui.time.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.reservation.application.time.dto.ReservationTimeCreateCommand;

public record ReservationTimeCreateRequest(
        @NotNull(message = "시간을 입력해주세요.") LocalTime startAt
) {

    public ReservationTimeCreateCommand convertToCreateCommand() {
        return new ReservationTimeCreateCommand(startAt);
    }
}
