package roomescape.domain.service;

import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationWaiting;

public record WaitingPromotionResult(
        ReservationWaiting targetWaiting,
        Reservation promotedReservation
) {
}
