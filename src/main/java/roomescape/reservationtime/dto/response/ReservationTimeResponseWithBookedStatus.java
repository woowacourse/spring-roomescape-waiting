package roomescape.reservationtime.dto.response;

import java.time.LocalTime;

public record ReservationTimeResponseWithBookedStatus(
    Long id,
    LocalTime startAt,
    boolean booked
) {

}
