package roomescape.domain.repository;

import roomescape.domain.Reservation;

public record ReservationWithRank(Reservation reservation, long rank) {
}
