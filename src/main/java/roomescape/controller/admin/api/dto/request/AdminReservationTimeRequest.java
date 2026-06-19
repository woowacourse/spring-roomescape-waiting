package roomescape.controller.admin.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.application.service.command.ReservationTimeCommand;

public record AdminReservationTimeRequest(
        @NotNull(message = "예약 시간 정보는 필수 값입니다.")
        LocalTime startAt
) {

    public ReservationTimeCommand toCommand() {
        return new ReservationTimeCommand(startAt);
    }
}
