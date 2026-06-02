package roomescape.service.dto;

import roomescape.domain.ReservationTime;

import java.time.LocalTime;

public record TimeAvailability(
        Long id,
        LocalTime startAt,
        boolean available
) {
    public static TimeAvailability from(ReservationTime time, boolean available) {
        return new TimeAvailability(time.getId(), time.getStartAt(), available);
    }
}
