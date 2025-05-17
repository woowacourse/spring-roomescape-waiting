package roomescape.reservation.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.reservation.service.dto.ReservationTimeCreateCommand;

public record ReservationTimeCreateRequest(@NotNull(message = "시간을 입력해주세요.") LocalTime startAt) {

    public ReservationTimeCreateCommand toCreateCommand() {
        return new ReservationTimeCreateCommand(startAt);
    }
}
