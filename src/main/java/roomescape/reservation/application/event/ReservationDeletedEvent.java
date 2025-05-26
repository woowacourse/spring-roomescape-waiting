package roomescape.reservation.application.event;

import roomescape.reservation.domain.Reservation;

public record ReservationDeletedEvent(Reservation reservation) {
}
