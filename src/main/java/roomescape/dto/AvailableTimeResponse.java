package roomescape.dto;

import java.time.LocalTime;

import roomescape.domain.Reservations;
import roomescape.entity.ReservationTime;

public record AvailableTimeResponse(long id, LocalTime startAt, boolean isBooked) {
    public static AvailableTimeResponse of(ReservationTime reservationTime, Reservations reservations) {
        return new AvailableTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                reservations.hasReservationTimeOf(reservationTime.getId())
        );
    }
}
