package roomescape.domain.vo;

import java.util.Optional;

import roomescape.domain.Reservation;

public record ReservationDeletion(Reservation deletedReservation, Optional<Reservation> promotedReservation) {
}
