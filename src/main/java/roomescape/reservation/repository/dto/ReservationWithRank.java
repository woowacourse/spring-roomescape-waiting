package roomescape.reservation.repository.dto;

import roomescape.reservation.domain.Reservation;

public record ReservationWithRank(
        Reservation reservation,
        long rank
) {
}
