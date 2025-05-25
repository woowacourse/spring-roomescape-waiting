package roomescape.reservation.application.event;

import java.util.Optional;
import roomescape.reservation.domain.Reservation;

public class ReservationDeletedEvent {
    private final Optional<Reservation> reservation;

    public ReservationDeletedEvent(Optional<Reservation> reservation) {
        this.reservation = reservation;
    }

    public Optional<Reservation> getReservation() {
        return reservation;
    }
}
