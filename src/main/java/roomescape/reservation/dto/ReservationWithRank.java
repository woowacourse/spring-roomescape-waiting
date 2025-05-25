package roomescape.reservation.dto;

import roomescape.reservation.domain.Reservation;

public record ReservationWithRank(
                Reservation reservation,
                long rank) {
}
