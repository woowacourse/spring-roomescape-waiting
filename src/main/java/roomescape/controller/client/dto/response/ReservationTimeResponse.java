package roomescape.controller.client.dto.response;

import java.time.LocalTime;
import roomescape.service.result.ReservationTimeResult;

public record ReservationTimeResponse(long id, LocalTime startAt) {

    public static ReservationTimeResponse from(ReservationTimeResult result) {
        return new ReservationTimeResponse(result.id(), result.startAt());
    }
}
