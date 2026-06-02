package roomescape.dto;

import roomescape.domain.ReservationTime;

import java.time.LocalTime;

public record TimeWithStatusResponse(
        Long id,
        LocalTime startAt,
        boolean reserved
) {
    public static TimeWithStatusResponse from(ReservationTime reservationTime, boolean reserved) {
        return new TimeWithStatusResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                reserved
        );
    }
}
