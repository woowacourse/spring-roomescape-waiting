package roomescape.dto;

import java.time.LocalTime;
import roomescape.entity.ReservationTime;
import roomescape.domain.Reservations;

public record AvailableTimeResponse(long id, LocalTime startAt, boolean isBooked) {
    public static AvailableTimeResponse of(ReservationTime reservationTime, Reservations reservations) {
        return new AvailableTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                reservations.hasReservationTimeOf(reservationTime.getId())
        );
    }
}
