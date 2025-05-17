package roomescape.reservation.application.dto;

import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public record AvailableReservationTimeResponse(
        Long id,
        LocalTime startAt,
        boolean isBooked
) {

    public static AvailableReservationTimeResponse from(final ReservationTime reservationTime, final boolean isBooked) {
        return new AvailableReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                isBooked);
    }
}
