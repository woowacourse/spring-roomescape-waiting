package roomescape.domain;

import roomescape.domain.reservation.Reservation;

public record ReservationWithRank(Reservation reservation, long rank) {
}
