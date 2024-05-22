package roomescape.dto;

import java.time.LocalTime;
import java.util.List;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;

public record AvailableTimeResponse(long id, LocalTime startAt, boolean isBooked) {
    public static AvailableTimeResponse of(ReservationTime reservationTime, List<Reservation> reservations) {
        return new AvailableTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                reservations.stream()
                        .anyMatch(reservation -> reservation.isReservationTimeOf(reservationTime.getId()))
        );
    }
}
