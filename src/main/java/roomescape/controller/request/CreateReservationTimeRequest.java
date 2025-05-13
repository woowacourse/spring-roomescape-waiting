package roomescape.controller.request;

import java.time.LocalTime;
import roomescape.service.param.CreateReservationTimeParam;

public record CreateReservationTimeRequest(
        LocalTime startAt
) {
    public CreateReservationTimeRequest {
        if (startAt == null) {
            throw new IllegalArgumentException("startAt은 필수값입니다.");
        }
    }

    public CreateReservationTimeParam toServiceParam() {
        return new CreateReservationTimeParam(startAt);
    }
}
