package roomescape.time.controller.dto;

import java.time.LocalTime;
import roomescape.time.service.dto.ReservationTimeResult;


public record ReservationTimeResponse(Long id, LocalTime startAt) {

    public static ReservationTimeResponse from(ReservationTimeResult time) {
        return new ReservationTimeResponse(time.id(), time.startAt());
    }
}
