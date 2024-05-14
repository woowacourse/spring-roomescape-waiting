package roomescape.service.response;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationTimeAppResponse(Long id, LocalTime startAt) {

    public static ReservationTimeAppResponse from(ReservationTime reservationTime) {
        return new ReservationTimeAppResponse(reservationTime.getId(), reservationTime.getStartAt());
    }
}
