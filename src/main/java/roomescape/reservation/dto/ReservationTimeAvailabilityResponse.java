package roomescape.reservation.dto;

import java.time.LocalTime;
import roomescape.time.domain.Time;

public record ReservationTimeAvailabilityResponse(long timeId, LocalTime startAt, boolean alreadyBooked) {

    public static ReservationTimeAvailabilityResponse fromTime(Time time, boolean alreadyBooked) {
        return new ReservationTimeAvailabilityResponse(time.getId(), time.getStartAt(), alreadyBooked);
    }

}
