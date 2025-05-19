package roomescape.reservationtime.dto.response;

import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservationTime;

public record ReservationTimeResponseWithBookedStatus(Long id, LocalTime startAt, boolean booked) {
    public static ReservationTimeResponseWithBookedStatus of(ReservationTime time, boolean booked) {
        return new ReservationTimeResponseWithBookedStatus(time.getId(), time.getStartAt(), booked);
    }
}
