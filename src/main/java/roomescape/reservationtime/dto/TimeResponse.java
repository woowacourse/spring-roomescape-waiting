package roomescape.reservationtime.dto;

import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservationTime;

public record TimeResponse(
        Long id,
        LocalTime startAt
) {

    public static TimeResponse of(ReservationTime saved) {
        return new TimeResponse(saved.getId(), saved.getStartAt());
    }
}