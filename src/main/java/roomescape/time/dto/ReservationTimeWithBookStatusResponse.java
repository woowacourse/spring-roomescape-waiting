package roomescape.time.dto;

import java.time.LocalTime;

public record ReservationTimeWithBookStatusResponse(Long timeId, LocalTime startAt, boolean alreadyBooked) {

}
