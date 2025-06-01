package roomescape.dto.response;

import java.time.LocalTime;
import roomescape.domain.reservationitem.ReservationTime;

public record ReservationTimeWithAvailabilityResponse(long id, LocalTime startAt, boolean isBooked) {

    public static ReservationTimeWithAvailabilityResponse from(ReservationTime time, boolean isBooked) {
        return new ReservationTimeWithAvailabilityResponse(time.getId(), time.getStartAt(), isBooked);
    }
}
