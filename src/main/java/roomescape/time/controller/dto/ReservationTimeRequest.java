package roomescape.time.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.time.service.dto.ReservationTimeCommand;

public record ReservationTimeRequest(
        @NotNull(message = "시간을 비운 채로 요청할 수 없습니다.")
        LocalTime startAt
) {
    public ReservationTimeCommand toCommand() {
        return new ReservationTimeCommand(startAt);
    }
}
