package roomescape.reservation.domain;

import java.util.Comparator;
import java.util.List;
import roomescape.reservation.domain.exception.NotSameSlotException;

public class ReservationSlot {

    private final List<Reservation> reservations;

    public ReservationSlot(List<Reservation> reservations) {
        validateSameSlot(reservations);
        this.reservations = reservations.stream()
                .sorted(Comparator.comparing(Reservation::getReservedAt))
                .toList();
    }

    private void validateSameSlot(List<Reservation> reservations) {
        Reservation first = reservations.getFirst();
        boolean isNotSameSlot = reservations.stream()
                .anyMatch(reservation ->
                        !reservation.getDate().equals(first.getDate())
                                || !reservation.getTimeId().equals(first.getTimeId())
                                || !reservation.getTheme().equals(first.getTheme())
                );
        if (isNotSameSlot) {
            throw new NotSameSlotException();
        }
    }

    public int getOrder(Reservation reservation) {
        return reservations.indexOf(reservation);
    }

    public boolean isFirst(Reservation reservation) {
        return getOrder(reservation) == 0;
    }

    public boolean hasWaiting() {
        return reservations.size() > 1;
    }

    public Reservation getNext(Reservation reservation) {
        int order = getOrder(reservation);
        return reservations.get(order + 1);
    }
}
