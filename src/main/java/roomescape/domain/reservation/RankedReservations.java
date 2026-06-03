package roomescape.domain.reservation;

import java.util.List;

public class RankedReservations {
    private final List<Reservation> reservations;

    public RankedReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public List<RankedReservation> resultsOf(String name) {
        List<Reservation> listByName = getListByName(name);

        return listByName.stream()
                .map(this::toRankedReservation)
                .toList();
    }

    private RankedReservation toRankedReservation(Reservation target) {
        List<Reservation> sameSlots = reservations.stream()
                .filter(reservation -> reservation.isSameSlot(target))
                .toList();

        return RankedReservation.decideRankFrom(target, sameSlots);
    }

    private List<Reservation> getListByName(String name) {
        return reservations.stream()
                .filter(reservation -> reservation.getName().equals(new ReservationName(name)))
                .toList();
    }

    public List<RankedReservation> resultsOf() {
        return reservations.stream()
                .map(this::toRankedReservation)
                .toList();
    }
}
