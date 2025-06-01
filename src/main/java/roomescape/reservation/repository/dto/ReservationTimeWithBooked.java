package roomescape.reservation.repository.dto;

import roomescape.reservation.domain.ReservationTime;

public record ReservationTimeWithBooked(
        ReservationTime time,
        boolean booked
) {
}
