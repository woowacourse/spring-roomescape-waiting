package roomescape.reservation.domain;

import java.util.Comparator;
import java.util.List;

public class ReservationSlot {

    private final List<Reservation> reservations;

    public ReservationSlot(List<Reservation> reservations) {
        this.reservations = reservations.stream()
                .sorted(Comparator.comparing(Reservation::getReservedAt))
                .toList();
    }

    public int getOrder(Reservation reservation) {
        return reservations.indexOf(reservation);
    }

    public boolean isFirst(Reservation reservation) {
        return getOrder(reservation) == 0;
    }

    public String getStatusDescription(Reservation reservation) {
        return isFirst(reservation) ? "예약" : getOrder(reservation) + "번째 예약 대기";
    }
}
