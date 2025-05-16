package roomescape.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;

public class ReservationSlots {

    private final List<ReservationSlot> reservationSlots;

    public ReservationSlots(
            List<ReservationTime> times,
            List<Reservation> alreadyReservedReservations
    ) {
        List<ReservationSlot> reservationSlots = new ArrayList<>();
        Set<ReservationTime> alreadyReservationTimes = alreadyReservedReservations.stream()
                .map(Reservation::getReservationTime)
                .collect(Collectors.toSet());

        for (ReservationTime time : times) {
            boolean contains = alreadyReservationTimes.contains(time);
            ReservationSlot reservationSlot = new ReservationSlot(time.getId(), time.getStartAt(), contains);
            reservationSlots.add(reservationSlot);
        }

        this.reservationSlots = reservationSlots;
    }

    public List<ReservationSlot> getReservationSlots() {
        return Collections.unmodifiableList(reservationSlots);
    }
}
