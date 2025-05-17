package roomescape.dto.reservationtime;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record AvailableTimeResponse(Long id, LocalTime startAt, boolean alreadyBooked) {

    public static AvailableTimeResponse from(ReservationTime reservationTime, boolean alreadyBooked) {
        return new AvailableTimeResponse(reservationTime.getId(), reservationTime.getStartAt(), alreadyBooked);
    }
}
