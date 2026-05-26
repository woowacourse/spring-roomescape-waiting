package roomescape.controller.admin.api.dto;

import java.time.LocalTime;
import roomescape.service.result.ReservationTimeResult;

public record AdminReservationTimeResponse(long id, LocalTime startAt, String status) {

    public static AdminReservationTimeResponse from(ReservationTimeResult result) {
        return new AdminReservationTimeResponse(result.id(), result.startAt(), result.status());
    }
}
