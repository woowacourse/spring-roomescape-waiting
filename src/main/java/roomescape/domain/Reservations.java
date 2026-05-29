package roomescape.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.Getter;

@Getter
public class Reservations {

    private final List<Reservation> reservations;

    public Reservations(List<Reservation> reservations) {
        this.reservations = new ArrayList<>(reservations);
    }

    public Reservation addReserved(String name, ReservationSlot slot) {
        Reservation reservation = Reservation.reserve(name, slot);
        reservations.add(reservation);
        return reservation;
    }

    public Reservation addWaiting(String name, ReservationSlot slot) {
        Reservation reservation = Reservation.waiting(name, slot);
        reservations.add(reservation);
        return reservation;
    }

    public boolean hasActiveReservationByName(String name) {
        return reservations.stream()
                .filter(reservation -> !reservation.isDeleted())
                .anyMatch(reservation -> reservation.hasSameName(name));
    }

    public boolean hasReservedReservation() {
        return reservations.stream()
                .anyMatch(Reservation::isReserved);
    }

    public List<Reservation> getReservations() {
        return List.copyOf(reservations);
    }

    public Optional<Reservation> findById(long id) {
        return reservations.stream()
                .filter(reservation -> reservation.isSameId(id))
                .findFirst();
    }

    public Optional<Reservation> findByNameAndStatus(String name, ReservationStatus status) {
        return reservations.stream()
                .filter(e -> e.matches(name, status))
                .findFirst();
    }

    public void promoteFirstWaiting() {
        reservations.stream()
                .filter(Reservation::isWaiting)
                .min(Comparator.comparing(Reservation::getCreatedAt))
                .ifPresent(Reservation::promote);
    }
}
