package roomescape.time.controller.dto;

import java.time.LocalTime;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.dto.AvailableTimeQueryResult;

public record ReservationTimeResponse(Long id, LocalTime startAt) {

    public static ReservationTimeResponse from(ReservationTime time) {
        return new ReservationTimeResponse(time.id(), time.startAt());
    }

    public static ReservationTimeResponse from(AvailableTimeQueryResult time) {
        return new ReservationTimeResponse(time.id(), time.startAt());
    }
}
