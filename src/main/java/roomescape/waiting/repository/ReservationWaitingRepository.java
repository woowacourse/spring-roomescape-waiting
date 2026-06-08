package roomescape.waiting.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.waiting.exception.ReservationWaitingErrorCode;
import roomescape.waiting.domain.ReservationWaiting;

@Repository
public class ReservationWaitingRepository {

    private final ReservationWaitingDao reservationWaitingDao;

    public ReservationWaitingRepository(ReservationWaitingDao reservationWaitingDao) {
        this.reservationWaitingDao = reservationWaitingDao;
    }

    public ReservationWaiting save(ReservationWaiting reservationWaiting) {
        try {
            return reservationWaitingDao.save(reservationWaiting);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ReservationWaitingErrorCode.DUPLICATE_WAITING);
        }
    }

    public Optional<ReservationWaiting> findById(long id) {
        return reservationWaitingDao.findById(id);
    }

    public List<ReservationWaiting> findAllByName(String name) {
        return reservationWaitingDao.findAllByName(name);
    }

    public void delete(ReservationWaiting reservationWaiting) throws NotFoundException {
        reservationWaitingDao.delete(reservationWaiting);
    }

    public boolean hasWaitingAtSameTime(String name, ReservationSlot reservationSlot) {
        return reservationWaitingDao.existsByDateAndTimeIdAndName(
                reservationSlot.date(),
                reservationSlot.time().getId(),
                name
        );
    }

    public List<ReservationWaiting> queryAllBySlotForUpdate(ReservationSlot slot) {
        return reservationWaitingDao.findAllByDateAndTimeIdAndThemeIdForUpdate(
                slot.date(),
                slot.time().getId(),
                slot.theme().getId()
        );
    }

    public List<ReservationWaiting> findAllBySlots(List<ReservationSlot> slots) {
        return reservationWaitingDao.findAllBySlots(slots);
    }
}
