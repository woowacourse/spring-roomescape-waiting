package roomescape.domain.reservation;

import java.util.LinkedList;
import java.util.List;
import java.util.SequencedCollection;
import roomescape.exception.AlreadyExistedException;

public class WaitingQueue {

    private final ReservationSlot slot;
    private final List<Reservation> queue;

    public WaitingQueue(final ReservationSlot slot, final SequencedCollection<Reservation> queue) {
        this.slot = slot;
        this.queue = createQueue(queue);
    }

    private List<Reservation> createQueue(final SequencedCollection<Reservation> reservations) {
        var queue = new LinkedList<Reservation>();
        reservations.forEach(reservation -> {
            checkSameSlot(reservation);
            queue.addLast(reservation);
        });
        return queue;
    }

    public int join(final Reservation reservation) {
        checkSameSlot(reservation);
        if (queue.contains(reservation)) {
            throw new AlreadyExistedException("이미 대기중인 예약입니다.");
        }
        queue.add(reservation);
        return queue.size();
    }

    public boolean areWaitingsEmpty() {
        return queue.isEmpty();
    }

    public int orderOf(final Reservation reservation) {
        checkSameSlot(reservation);
        if (areWaitingsEmpty() || !queue.contains(reservation)) {
            throw new IllegalArgumentException("해당 예약은 대기열에 존재하지 않습니다.");
        }
        return queue.indexOf(reservation) + 1;
    }

    private void checkSameSlot(final Reservation reservation) {
        if (!slot.equals(reservation.slot())) {
            throw new IllegalArgumentException("예약 슬롯이 일치하지 않습니다 : " + reservation);
        }
    }
}
