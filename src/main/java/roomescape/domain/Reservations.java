package roomescape.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Reservations {

    private final List<Reservation> reservations;

    public Reservations(List<Reservation> reservations) {
        this.reservations = new ArrayList<>(reservations);
    }

    public Reservation addReserved(String name) {
        Reservation reservation = Reservation.reserve(name);
        reservations.add(reservation);
        return reservation;
    }

    public Reservation addWaiting(String name) {
        Reservation reservation = Reservation.waiting(name);
        reservations.add(reservation);
        return reservation;
    }

    public Reservation addPending(String name, ReservationStatus status) {
        Reservation reservation = Reservation.pending(name, status);
        reservations.add(reservation);
        return reservation;
    }

    public void promoteFirstWaiting() {
        reservations.stream()
                .filter(Reservation::isActiveWaiting)
                .min(Comparator.comparing(Reservation::getCreatedAt))
                .ifPresent(Reservation::promote);
    }

    public List<Reservation> getReservations() {
        return List.copyOf(reservations);
    }

    public boolean hasReservationByName(String name) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.hasSameActiveName(name));
    }

    public boolean hasReservedReservation() {
        return reservations.stream()
                .anyMatch(Reservation::isActiveReserved);
    }

    public Optional<Reservation> findById(long id) {
        return reservations.stream()
                .filter(reservation -> reservation.isSameId(id))
                .findFirst();
    }

    public Optional<Reservation> findActiveById(long id) {
        return reservations.stream()
                .filter(reservation -> reservation.isActiveWithId(id))
                .findFirst();
    }

    public Optional<Reservation> findActiveEntryByName(String name) {
        return reservations.stream()
                .filter(reservation -> reservation.hasSameActiveName(name))
                .findFirst();
    }

    public Optional<Reservation> findLatestPendingEntryByName(String name) {
        return reservations.stream()
                .filter(reservation -> reservation.isPendingWithName(name))
                .max(Comparator.comparing(Reservation::getCreatedAt));
    }
}
