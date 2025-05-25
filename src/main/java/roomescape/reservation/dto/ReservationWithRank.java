package roomescape.reservation.dto;

import roomescape.reservation.domain.Reservation;

public record ReservationWithRank(Reservation waiting, int rank) {
}
