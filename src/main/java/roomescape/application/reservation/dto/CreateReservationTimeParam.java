package roomescape.application.reservation.dto;

import java.time.LocalTime;

public record CreateReservationTimeParam(
        LocalTime startAt
) {
}
