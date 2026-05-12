package roomescape.domain;

import roomescape.domain.ReservationTime;

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
}
