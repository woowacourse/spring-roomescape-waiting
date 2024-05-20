package roomescape.service.dto.response.reservationTime;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationStatusResponse(LocalTime startAt, Long timeId, boolean alreadyBooked) {

    public ReservationStatusResponse(ReservationTime reservationTime, boolean alreadyBooked) {
        this(reservationTime.getStartAt(), reservationTime.getId(), alreadyBooked);
    }
}
