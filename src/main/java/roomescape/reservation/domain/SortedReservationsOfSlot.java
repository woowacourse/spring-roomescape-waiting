package roomescape.reservation.domain;

import java.util.Comparator;
import java.util.List;

public class SortedReservationsOfSlot {

    private final Reservations reservations;

    private SortedReservationsOfSlot(final ReservationSlot reservationSlot, final List<Reservation> reservations) {
        for (final Reservation r : reservations) {
            requiredSameSlot(r, reservationSlot);
        }
        final List<Reservation> sorted = reservations.stream()
                .sorted(Comparator.comparing(Reservation::getCreatedAt))
                .toList();

        this.reservations = new Reservations(sorted);
    }

    public static SortedReservationsOfSlot of(final ReservationSlot reservationSlot, final List<Reservation> reservations) {
        return new SortedReservationsOfSlot(reservationSlot, reservations);
    }

    public Reservation getNextApprovableReservation() {
        return null;
    }

    public int indexOf(final Reservation reservation) {
        return reservations.indexOf(reservation);
    }

    public boolean isEmpty() {
        return reservations.isEmpty();
    }

    private void requiredSameSlot(final Reservation reservation, final ReservationSlot reservationSlot) {
        if (reservationSlot.isSame(ReservationSlot.from(reservation))) {
            return;
        }
        throw new IllegalArgumentException("Provided reservationSlot does not match the reservation's reservationSlot. (expected: %s, actual: %s)");
    }
}
