package roomescape.reservation.controller.response;

import roomescape.reservation.model.ReservationTime;

import java.time.LocalTime;

public record AvailableReservationTimeResponse(
        Long timeId,
        LocalTime startAt,
        boolean alreadyBooked
) {
    public static AvailableReservationTimeResponse of(
            final ReservationTime reservationTime,
            final boolean alreadyBooked
    ) {
        return new AvailableReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                alreadyBooked);
    }
}
