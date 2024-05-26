package roomescape.reservation.domain;

import java.util.List;

public class Reservations {

    private final List<Reservation> reservations;

    public Reservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public boolean hasReservationMadeBy(Long memberId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.isReservedBy(memberId));
    }
}
