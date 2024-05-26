package roomescape.domain.reservation.dto;

import roomescape.domain.reservation.domain.reservation.Reservation;

public record ReservationWithOrderDto(Reservation reservation, Long orderNumber) {
}
