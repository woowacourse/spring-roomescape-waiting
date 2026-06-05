package roomescape.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservationtime.ReservationTimeDao;
import roomescape.reservationwait.ReservationWaitDao;
import roomescape.member.Member;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservation.exception.ReservationAlreadyExistsException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservationtime.exception.ReservationTimeNotFoundException;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ReservationWaitDao reservationWaitDao;

    public ReservationService(ReservationDao reservationDao, ReservationTimeDao reservationTimeDao,
                              ReservationWaitDao reservationWaitDao) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.reservationWaitDao = reservationWaitDao;
    }

    public List<Reservation> getReservations(Long memberId) {
        return reservationDao.findAllReservationsByMemberId(memberId);
    }

    public List<Reservation> findReservationsByStoreId(Long storeId) {
        return reservationDao.findReservationsByStoreId(storeId);
    }

    @Transactional
    public Reservation createReservation(Long memberId, LocalDate date, Long timeId, Long themeId, Long storeId) {
        ReservationTime time = findReservationTime(timeId);
        Reservation candidate = Reservation.create(memberId, date, time, themeId, storeId);
        try {
            return reservationDao.insert(candidate);
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistsException();
        }
    }

    @Transactional
    public Reservation updateReservation(Long id, LocalDate date, Long memberId, Long timeId) {
        Reservation existing = findReservation(id);
        ReservationTime newTime = findReservationTime(timeId);
        Reservation updated = existing.changeTo(memberId, date, newTime);
        try {
            return reservationDao.update(updated);
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistsException();
        }
    }

    @Transactional
    public Reservation updateReservationByManager(Long reservationId, LocalDate date, Long timeId, Member manager) {
        Reservation existing = findReservation(reservationId);
        ReservationTime newTime = findReservationTime(timeId);
        Reservation updated = existing.changeToByManager(manager, date, newTime);
        try {
            return reservationDao.update(updated);
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistsException();
        }
    }

    @Transactional
    public void deleteReservation(Long reservationId, Long memberId) {
        Reservation reservation = findReservation(reservationId);
        reservation.cancelBy(memberId);
        deleteOrPromoteWait(reservation);
    }

    @Transactional
    public void deleteReservationByManager(Long reservationId, Member manager) {
        Reservation reservation = findReservation(reservationId);
        reservation.validateStoreOwnership(manager);
        deleteOrPromoteWait(reservation);
    }

    private void deleteOrPromoteWait(Reservation reservation) {
        if (reservation.isPast()) {
            reservationWaitDao.deleteAllByReservationId(reservation.getId());
            reservationDao.delete(reservation.getId());
            return;
        }
        reservationWaitDao.findEarliestMemberIdForUpdate(reservation.getId())
                .ifPresentOrElse(
                        waiterId -> promote(reservation, waiterId),
                        () -> reservationDao.delete(reservation.getId()));
    }

    private void promote(Reservation reservation, Long waiterId) {
        reservationDao.update(reservation.promoteTo(waiterId));
        reservationWaitDao.deleteByReservationIdAndMemberId(reservation.getId(), waiterId);
    }

    private Reservation findReservation(Long id) {
        try {
            return reservationDao.findReservationById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ReservationNotFoundException();
        }
    }

    private ReservationTime findReservationTime(Long id) {
        try {
            return reservationTimeDao.findReservationTimeById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ReservationTimeNotFoundException();
        }
    }
}
