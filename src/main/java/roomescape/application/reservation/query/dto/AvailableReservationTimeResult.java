package roomescape.application.reservation.query.dto;

import java.time.LocalTime;

public record AvailableReservationTimeResult(
        Long timeId,
        LocalTime startAt,
        boolean booked
) {
}
