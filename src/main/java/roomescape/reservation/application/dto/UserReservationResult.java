package roomescape.reservation.application.dto;

import java.util.List;
import roomescape.reservation.domain.Reservation;
import roomescape.waiting.domain.Waiting;

public record UserReservationResult(
        List<Reservation> reservations,
        List<Waiting> waitings
) {
    public static UserReservationResult from(List<Reservation> reservations, List<Waiting> waitings) {
        return new UserReservationResult(reservations, waitings);
    }
}
