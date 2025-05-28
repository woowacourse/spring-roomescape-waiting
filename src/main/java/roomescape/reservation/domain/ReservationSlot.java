package roomescape.reservation.domain;

import java.util.Comparator;
import java.util.List;
import roomescape.reservation.domain.exception.NotExistsWaitingException;
import roomescape.reservation.domain.exception.NotSameSlotException;

public class ReservationSlot {

    private final List<Reservation> reservations;

    public ReservationSlot(List<Reservation> reservations) {
        validateSameSlot(reservations);
        this.reservations = reservations.stream()
                .sorted(Comparator.comparing(Reservation::getReservedAt))
                .toList();
    }

//    public ReservationSlot(List<Reservation> reservations, List<Waiting> waitings) {
//        this.reservations = reservations.stream()
//                .sorted(Comparator.comparing(Reservation::getReservedAt))
//                .toList();
//        this.reservations.addAll(waitings.stream()
//                .map(Waiting::toReservation)
//                .sorted(Comparator.comparing(Reservation::getReservedAt))
//                .toList());
//    }

    private void validateSameSlot(List<Reservation> reservations) {
        Reservation first = reservations.getFirst();
        boolean isNotSameSlot = reservations.stream()
                .anyMatch(reservation ->
                        !reservation.getDate().equals(first.getDate())
                                || !reservation.getStartAt().equals(first.getStartAt())
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
        if (!hasWaiting()) {
            throw new NotExistsWaitingException();
        }
        int order = getOrder(reservation);
        return reservations.get(order + 1);
    }
}
