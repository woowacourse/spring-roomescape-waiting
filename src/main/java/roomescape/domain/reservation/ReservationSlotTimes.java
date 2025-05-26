package roomescape.domain.reservation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.waiting.Waiting;

public class ReservationSlotTimes {

    private final List<ReservationSlot> reservationSlots;

    public ReservationSlotTimes(List<ReservationTime> times, List<Reservation> alreadyReservedReservations, List<Waiting> alreadyWaitings) {
        List<ReservationSlot> reservationSlots = new ArrayList<>();
        Set<ReservationTime> alreadyReservationTimes = Stream.concat(
                alreadyReservedReservations.stream().map(Reservation::getReservationTime),
                alreadyWaitings.stream().map(Waiting::getTime)
        ).collect(Collectors.toSet());

        for (ReservationTime time : times) {
            boolean contains = alreadyReservationTimes.contains(time);
            ReservationSlot reservationSlot = new ReservationSlot(time.getId(), time.getTime(),
                    contains);
            reservationSlots.add(reservationSlot);
        }

        this.reservationSlots = reservationSlots;
    }

    public List<ReservationSlot> getAvailableBookTimes() {
        return Collections.unmodifiableList(reservationSlots);
    }
}
