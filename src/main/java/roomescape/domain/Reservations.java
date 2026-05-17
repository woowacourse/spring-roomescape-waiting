package roomescape.domain;

import java.util.List;

public class Reservations {

    private final List<Reservation> reservations;

    public Reservations(List<Reservation> reservations) {
        this.reservations = List.copyOf(reservations);
    }

    public boolean isOccupied(ReservationTime time) {
        return reservations.stream()
                .anyMatch(r -> r.getTime().equals(time));
    }

    public Reservations excluding(Long reservationId) {
        return new Reservations(reservations.stream()
                .filter(r -> !reservationId.equals(r.getId()))
                .toList());
    }
}
