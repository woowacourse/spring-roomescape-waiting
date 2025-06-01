package roomescape.dto.response;

import java.time.LocalTime;
import roomescape.domain.reservationitem.ReservationTime;

public record ReservationTimeResponse(long id, LocalTime startAt) {

    public static ReservationTimeResponse from(ReservationTime reservationTime) {
        return new ReservationTimeResponse(reservationTime.getId(), reservationTime.getStartAt());
    }
}
