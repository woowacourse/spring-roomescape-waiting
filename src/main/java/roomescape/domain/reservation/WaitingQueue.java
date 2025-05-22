package roomescape.domain.reservation;

import java.util.Comparator;
import java.util.List;
import java.util.SequencedCollection;

public class WaitingQueue {

    private final List<Reservation> queue;

    public WaitingQueue(final SequencedCollection<Reservation> reservations) {
        this.queue = sorted(reservations);
    }

    private List<Reservation> sorted(final SequencedCollection<Reservation> reservations) {
        return reservations.stream()
            .sorted(Comparator.comparing(Reservation::id))
            .toList();
    }

    public int orderOf(final Reservation reservation) {
        if (queue.isEmpty() || !queue.contains(reservation)) {
            throw new IllegalArgumentException("해당 예약은 대기열에 존재하지 않습니다 : " + reservation);
        }
        return queue.indexOf(reservation) + 1;
    }
}
