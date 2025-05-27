package roomescape.domain.reservation;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.RoomescapeSchedule;

public class ReservationQueues {

    private final Map<RoomescapeSchedule, ReservationQueue> queues;

    public ReservationQueues(final List<Reservation> reservations) {
        this.queues = reservations.stream()
            .collect(groupingBy(
                Reservation::reservedSchedule,
                collectingAndThen(toList(), ReservationQueue::new)
            ));
    }

    public int orderOf(final Reservation reservation) {
        if (!reservation.isWaiting()) {
            return ReservationQueue.ORDERING_START_INDEX;
        }
        var queue = queueOfSchedule(reservation.reservedSchedule());
        return queue.orderOf(reservation);
    }

    public List<ReservationWithOrder> orderOfAll(List<Reservation> reservations) {
        return reservations.stream()
            .map(r -> new ReservationWithOrder(r, orderOf(r)))
            .sorted(comparing(ReservationWithOrder::order))
            .toList();
    }

    public Optional<Reservation> findNext(final Reservation reservation) {
        var queue = queueOfSchedule(reservation.reservedSchedule());
        return queue.findNext(reservation);
    }

    private ReservationQueue queueOfSchedule(final RoomescapeSchedule schedule) {
        return queues.getOrDefault(schedule, new ReservationQueue(Collections.emptyList()));
    }
}
