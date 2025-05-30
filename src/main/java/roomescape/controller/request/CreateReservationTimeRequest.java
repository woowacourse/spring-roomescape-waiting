package roomescape.controller.request;

import jakarta.validation.constraints.NotNull;
import roomescape.service.param.CreateReservationTimeParam;

import java.time.LocalTime;

public record CreateReservationTimeRequest(
        @NotNull(message = "예약 시간은 필수 값입니다.")
        LocalTime startAt
) {
    public CreateReservationTimeParam toServiceParam() {
        return new CreateReservationTimeParam(startAt);
    }
}
