package roomescape.reservation.domain;

import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class Reservations {

    private final List<Reservation> reservations;

    public static Reservations from(final List<Reservation> reservations) {
        return new Reservations(reservations);
    }

    public Reservation getFirst() {
        return reservations.getFirst();
    }

    public int indexOf(final Reservation reservation) {
        return reservations.indexOf(reservation);
    }

    public int size() {
        return reservations.size();
    }

    public boolean isEmpty() {
        return reservations.isEmpty();
    }

    public List<Reservation> getReservations() {
        return Collections.unmodifiableList(reservations);
    }
}
