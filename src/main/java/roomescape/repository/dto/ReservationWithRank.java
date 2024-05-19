package roomescape.repository.dto;

import roomescape.domain.reservation.Reservation;

public record ReservationWithRank(Reservation reservation, Long rank) { }
