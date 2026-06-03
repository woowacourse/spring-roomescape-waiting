package roomescape.waiting.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.domain.ReservationWaitingRepository;

@Repository
public class ReservationWaitingRepositoryImpl implements ReservationWaitingRepository {

    private final ReservationWaitingDao reservationWaitingDao;

    public ReservationWaitingRepositoryImpl(ReservationWaitingDao reservationWaitingDao) {
        this.reservationWaitingDao = reservationWaitingDao;
    }

    @Override
    public ReservationWaiting save(ReservationWaiting reservationWaiting) {
        return reservationWaitingDao.save(reservationWaiting);
    }

    @Override
    public Optional<ReservationWaiting> findById(Long id) {
        return reservationWaitingDao.findById(id);
    }

    @Override
    public List<ReservationWaiting> findAllByName(String name) {
        return reservationWaitingDao.findAllByName(name);
    }

    @Override
    public void delete(ReservationWaiting reservationWaiting) throws NotFoundException {
        reservationWaitingDao.delete(reservationWaiting);
    }

    @Override
    public boolean hasWaitingAtSameTime(ReservationWaiting reservationWaiting) {
        return reservationWaitingDao.existsByDateAndTimeIdAndName(
                reservationWaiting.getDate(),
                reservationWaiting.getTime().getId(),
                reservationWaiting.getName()
        );
    }

    @Override
    public List<ReservationWaiting> queryAllBySlotForUpdate(ReservationSlot slot) {
        return reservationWaitingDao.findAllByDateAndTimeIdAndThemeIdForUpdate(
                slot.date(),
                slot.time().getId(),
                slot.theme().getId()
        );
    }
}
