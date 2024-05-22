package roomescape.domain;

import java.util.Collections;
import java.util.List;

import roomescape.entity.Reservation;

public class Reservations {
    private final List<Reservation> reservations;

    public Reservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public boolean hasSameReservation(Reservation otherReservation) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.isSameReservation(otherReservation));
    }

    public boolean hasReservationTimeOf(long timeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.isReservationTimeOf(timeId));
    }

    public List<Reservation> waiting(){
        return reservations.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.WAITING)
                .toList();
    }

    public List<Reservation> booked(){
        return reservations.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.BOOKED)
                .toList();
    }

    public List<Reservation> getReservations() {
        return Collections.unmodifiableList(reservations);
    }
}
