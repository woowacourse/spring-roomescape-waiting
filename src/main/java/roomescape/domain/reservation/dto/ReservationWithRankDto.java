package roomescape.domain.reservation.dto;

import roomescape.domain.reservation.Reservation;

public record ReservationWithRankDto(Reservation reservation, Long rank) {
}
