package roomescape.domain.reservation;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReservationQueues {

    private final Map<ReservationSlot, ReservationQueue> queues;

    public ReservationQueues(final List<Reservation> reservations) {
        this.queues = reservations.stream()
            .collect(groupingBy(
                Reservation::slot,
                collectingAndThen(toList(), ReservationQueue::new)
            ));
    }

    public int orderOf(final Reservation reservation) {
        if (!reservation.isWaiting()) {
            return ReservationQueue.ORDERING_START_INDEX;
        }
        var queue = queueOfSlot(reservation.slot());
        return queue.orderOf(reservation);
    }

    public List<ReservationWithOrder> orderOfAll(List<Reservation> reservations) {
        return reservations.stream()
            .map(r -> new ReservationWithOrder(r, orderOf(r)))
            .toList();
    }

    public Optional<Reservation> findNext(final Reservation reservation) {
        var queue = queueOfSlot(reservation.slot());
        return queue.findNext(reservation);
    }

    private ReservationQueue queueOfSlot(final ReservationSlot slot) {
        return queues.getOrDefault(slot, new ReservationQueue(Collections.emptyList()));
    }
}
