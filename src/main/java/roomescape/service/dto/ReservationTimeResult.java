package roomescape.service.dto;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationTimeResult(Long id, LocalTime startAt) {
    public static ReservationTimeResult from(ReservationTime time) {
        return new ReservationTimeResult(time.getId(), time.getStartAt());
    }
}
