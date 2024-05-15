package roomescape.time.dto;

import java.time.LocalTime;
import roomescape.time.domain.ReservationTime;

public record AvailableTimeResponse(Long timeId, LocalTime startAt, boolean alreadyBooked) {

    public AvailableTimeResponse(ReservationTime reservationTime, boolean alreadyBooked) {
        this(reservationTime.getId(), reservationTime.getStartAt(), alreadyBooked);
    }

}
