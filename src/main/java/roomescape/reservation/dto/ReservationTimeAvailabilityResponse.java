package roomescape.reservation.dto;

import java.time.LocalTime;
import roomescape.time.domain.ReservationTime;

public record ReservationTimeAvailabilityResponse(long timeId, LocalTime startAt, boolean alreadyBooked) {

    public static ReservationTimeAvailabilityResponse fromTime(ReservationTime time, boolean alreadyBooked) {
        return new ReservationTimeAvailabilityResponse(time.getId(), time.getStartAt(), alreadyBooked);
    }
}
