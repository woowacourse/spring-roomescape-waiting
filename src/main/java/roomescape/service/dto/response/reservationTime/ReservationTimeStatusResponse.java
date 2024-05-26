package roomescape.service.dto.response.reservationTime;

import java.time.LocalTime;
import roomescape.domain.reservationtime.ReservationTime;

public record ReservationTimeStatusResponse(LocalTime startAt, Long timeId, boolean alreadyBooked) {

    public ReservationTimeStatusResponse(ReservationTime reservationTime, boolean alreadyBooked) {
        this(reservationTime.getStartAt(), reservationTime.getId(), alreadyBooked);
    }
}
