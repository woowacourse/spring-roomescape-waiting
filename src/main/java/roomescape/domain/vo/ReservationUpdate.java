package roomescape.domain.vo;

import java.util.Optional;

import roomescape.domain.Reservation;

public record ReservationUpdate(Reservation updatedReservation, Optional<Reservation> promotedReservation) {
}
