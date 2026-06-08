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
import roomescape.reservationhistory.ReservationHistory;
import roomescape.reservationhistory.ReservationHistoryAction;
import roomescape.reservationhistory.ReservationHistoryDao;
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
    private final ReservationHistoryDao reservationHistoryDao;

    public ReservationService(ReservationDao reservationDao, ReservationTimeDao reservationTimeDao,
                              ReservationWaitDao reservationWaitDao,
                              ReservationHistoryDao reservationHistoryDao) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.reservationWaitDao = reservationWaitDao;
        this.reservationHistoryDao = reservationHistoryDao;
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
            Reservation saved = reservationDao.insert(candidate);
            reservationHistoryDao.insert(
                    ReservationHistory.of(saved, ReservationHistoryAction.CREATED, memberId));
            return saved;
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
            Reservation saved = reservationDao.update(updated);
            reservationHistoryDao.insert(
                    ReservationHistory.of(saved, ReservationHistoryAction.UPDATED, memberId));
            return saved;
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
            Reservation saved = reservationDao.update(updated);
            reservationHistoryDao.insert(
                    ReservationHistory.of(saved, ReservationHistoryAction.UPDATED, manager.getId()));
            return saved;
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistsException();
        }
    }

    @Transactional
    public void deleteReservation(Long reservationId, Long memberId) {
        Reservation reservation = findReservation(reservationId);
        reservation.cancelBy(memberId);
        deleteOrPromoteWait(reservation, memberId);
    }

    @Transactional
    public void deleteReservationByManager(Long reservationId, Member manager) {
        Reservation reservation = findReservation(reservationId);
        reservation.validateStoreOwnership(manager);
        deleteOrPromoteWait(reservation, manager.getId());
    }

    private void deleteOrPromoteWait(Reservation reservation, Long actorId) {
        if (reservation.isPast()) {
            reservationWaitDao.deleteAllByReservationId(reservation.getId());
            reservationDao.delete(reservation.getId());
            reservationHistoryDao.insert(
                    ReservationHistory.of(reservation, ReservationHistoryAction.CANCELED, actorId));
            return;
        }
        reservationWaitDao.findEarliestMemberIdForUpdate(reservation.getId())
                .ifPresentOrElse(
                        waiterId -> promote(reservation, waiterId, actorId),
                        () -> {
                            reservationDao.delete(reservation.getId());
                            reservationHistoryDao.insert(
                                    ReservationHistory.of(reservation, ReservationHistoryAction.CANCELED, actorId));
                        });
    }

    private void promote(Reservation reservation, Long waiterId, Long actorId) {
        Reservation promoted = reservation.promoteTo(waiterId);
        reservationDao.update(promoted);
        reservationWaitDao.deleteByReservationIdAndMemberId(reservation.getId(), waiterId);
        reservationHistoryDao.insert(
                ReservationHistory.of(reservation, ReservationHistoryAction.TRANSFERRED_OUT, actorId));
        reservationHistoryDao.insert(
                ReservationHistory.of(promoted, ReservationHistoryAction.TRANSFERRED_IN, actorId));
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
