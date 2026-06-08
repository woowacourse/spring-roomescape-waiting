package roomescape.service.event;

import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import roomescape.dao.ReservationDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.slot.EventSlot;
import roomescape.domain.waiting.Waiting;
import roomescape.infrastructure.SlotManager;

@Component
public class ReservationChangeListener {
    private final WaitingDao waitingDao;
    private final ReservationDao reservationDao;
    private final SlotManager slotManager;

    public ReservationChangeListener(
            WaitingDao waitingDao,
            ReservationDao reservationDao,
            SlotManager slotManager
    ) {
        this.waitingDao = waitingDao;
        this.reservationDao = reservationDao;
        this.slotManager = slotManager;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void promoteWaitingUser(ReservationChangeEvent event) {
        EventSlot slot = event.eventSlot();

        Optional<Waiting> firstWaiting = waitingDao.findByEventSlot(slot);

        if (firstWaiting.isEmpty()) {
            slotManager.release(slot);
            return;
        }

        Waiting waiting = firstWaiting.get();

        Reservation pendingReservation = Reservation.createPending(
                waiting.getName(),
                slot.date(),
                slot.time(),
                slot.theme()
        );

        reservationDao.save(pendingReservation);
        waitingDao.delete(waiting.getId());
    }
}
