package roomescape.reservation.dto;

import java.time.LocalTime;

public record AvailableReservationTimeResponse(
        Long timeId,
        LocalTime startAt,
        boolean alreadyBooked
) {
}
