package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.dao.ReservationDao;
import roomescape.dao.SlotDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.domain.Waiting;
import roomescape.exception.code.SlotErrorCode;
import roomescape.exception.domain.SlotException;

import java.util.Optional;

@Service
public class WaitingPromotionService {

    private final WaitingDao waitingDao;
    private final SlotDao slotDao;
    private final ReservationDao reservationDao;

    public WaitingPromotionService(WaitingDao waitingDao, SlotDao slotDao, ReservationDao reservationDao) {
        this.waitingDao = waitingDao;
        this.slotDao = slotDao;
        this.reservationDao = reservationDao;
    }

    public void promoteWaiting(Long slotId) {
        Optional<Waiting> topWaiting = waitingDao.findFirstBySlotIdOrderByCreatedAt(slotId);

        Slot slot = slotDao.findById(slotId)
                .orElseThrow(() -> new SlotException(SlotErrorCode.SLOT_NOT_FOUND));

        if (topWaiting.isPresent()) {
            Reservation newReservation = new Reservation(slot, topWaiting.get().getName());
            reservationDao.save(newReservation);
            waitingDao.delete(topWaiting.get().getId());
        }
    }
}
