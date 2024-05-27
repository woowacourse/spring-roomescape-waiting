package roomescape.reservation.domain;

import java.util.Comparator;
import java.util.List;

public class Reservations {

    private final List<Reservation> reservationsOnQueue;

    public Reservations(List<Reservation> reservationsOnQueue) {
        reservationsOnQueue.sort(Comparator.comparing(Reservation::getCreatedAt));
        this.reservationsOnQueue = reservationsOnQueue;
    }

    public int getReservationRank(Reservation reservation) {
        return reservationsOnQueue.indexOf(reservation);
    }

    public List<Reservation> getReservations() {
        return List.copyOf(reservationsOnQueue);
    }
}
