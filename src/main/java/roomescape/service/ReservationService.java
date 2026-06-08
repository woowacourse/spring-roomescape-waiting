package roomescape.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ReservationWaitDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWait;
import roomescape.dto.result.CreatedWaitResult;
import roomescape.dto.result.ReservationResult;
import roomescape.dto.result.StoreReservationResult;
import roomescape.dto.result.WaitingResponseResult;
import roomescape.exception.reservation.*;
import roomescape.exception.reservationtime.ReservationTimeNotFoundException;
import roomescape.exception.reservationwait.PastReservationWaitNotAllowedException;
import roomescape.exception.reservationwait.ReservationWaitAlreadyExistsException;
import roomescape.exception.reservationwait.SelfReservationWaitNotAllowedException;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ReservationWaitDao reservationWaitDao;

    public ReservationService(ReservationDao reservationDao, ReservationTimeDao reservationTimeDao, ReservationWaitDao reservationWaitDao) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.reservationWaitDao = reservationWaitDao;
    }

    public List<ReservationResult> getReservations(Long memberId) {
        return reservationDao.findAllReservationsByMemberId(memberId);
    }

    public List<WaitingResponseResult> getWaitings(Long memberId) {
        return reservationWaitDao.findWaitingsByMemberId(memberId).stream()
                .map(WaitingResponseResult::from)
                .toList();
    }

    public List<StoreReservationResult> findByStoreId(Long storeId) {
        return reservationDao.findByStoreId(storeId);
    }

    @Transactional
    public ReservationResult createReservation(Long memberId, LocalDate date, Long timeId, Long themeId, Long storeId) {
        ReservationTime reservationTime = reservationTimeDao.findReservationTimeById(timeId);
        if (reservationTime.isPast(date)) {
            throw new PastReservationNotAllowedException();
        }
        try {
            Long id = reservationDao.insertWithKeyHolder(memberId, date, timeId, themeId, storeId);
            return reservationDao.findReservationResultById(id);
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistsException();
        }
    }

    @Transactional
    public CreatedWaitResult createWait(Long memberId, Long reservationId) {
        Reservation reservation = findReservationByIdForUpdate(reservationId);
        if (reservation.isPast()) {
            throw new PastReservationWaitNotAllowedException();
        }
        validateIfSelfReserved(memberId, reservation);
        try {
            Long waitId = reservationWaitDao.createReservationWait(memberId, reservationId);
            ReservationWait wait = reservationWaitDao.findReservationWaitById(waitId)
                    .orElseThrow(() -> new IllegalStateException(
                            "방금 생성한 예약 대기를 조회할 수 없습니다. waitId=" + waitId));
            Long order = reservationWaitDao.findWaitOrder(waitId);
            return new CreatedWaitResult(wait, order);
        } catch (DuplicateKeyException e) {
            throw new ReservationWaitAlreadyExistsException();
        }
    }

    private Reservation findReservationByIdForUpdate(Long reservationId) {
        reservationDao.lockById(reservationId);
        Reservation reservation = findReservation(reservationId);
        return reservation;
    }

    private Reservation findReservation(Long id) {
        try {
            return reservationDao.findReservationById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ReservationNotFoundException();
        }
    }

    private static void validateIfSelfReserved(Long memberId, Reservation reservation) {
        if (reservation.getMemberId().equals(memberId)) {
            throw new SelfReservationWaitNotAllowedException();
        }
    }

    @Transactional
    public ReservationResult updateReservation(Long id, LocalDate date, Long memberId, Long timeId) {
        ReservationTime reservationTime = findReservationTime(timeId);
        validateReservationOwner(memberId, findReservation(id));
        if (reservationTime.isPast(date)) {
            throw new PastReservationNotAllowedException();
        }
        try {
            reservationDao.updateById(id, date, timeId);
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistsException();
        }
        return reservationDao.findReservationResultById(id);
    }

    private void validateReservationOwner(Long memberId, Reservation reservation) {
        if (!reservation.isReservedBy(memberId)) {
            throw new ReservationOwnerMismatchException();
        }
    }

    private ReservationTime findReservationTime(Long id) {
        try {
            return reservationTimeDao.findReservationTimeById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ReservationTimeNotFoundException();
        }
    }

    @Transactional
    public ReservationResult updateByManager(Long reservationId, LocalDate date, Long timeId, Member manager) {
        ReservationTime reservationTime = findReservationTime(timeId);
        Reservation reservation = findReservation(reservationId);
        reservation.validateStoreOwnership(manager);
        if (reservationTime.isPast(date)) {
            throw new PastReservationNotAllowedException();
        }
        try {
            reservationDao.updateById(reservationId, date, timeId);
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistsException();
        }
        return reservationDao.findReservationResultById(reservationId);
    }

    @Transactional
    public void deleteReservation(Long reservationId, Long memberId) {
        Reservation reservation = findReservationByIdForUpdate(reservationId);
        validateReservationOwner(memberId, reservation);
        if (reservation.isPast()) {
            throw new PastReservationCancelNotAllowedException();
        }
        deleteOrPromoteWaiting(reservationId);
    }

    private void deleteOrPromoteWaiting(Long reservationId) {
        reservationWaitDao.findEarliestMemberId(reservationId)
                .ifPresentOrElse(
                        m -> changeToRecentWaitingMember(reservationId, m),
                        () -> reservationDao.delete(reservationId));
    }

    private void changeToRecentWaitingMember(Long reservationId, Long memberId) {
        reservationDao.updateMemberId(reservationId, memberId);
        reservationWaitDao.deleteByReservationIdAndMemberId(reservationId, memberId);
    }

    @Transactional
    public void deleteByManager(Long reservationId, Member manager) {
        Reservation reservation = findReservation(reservationId);
        reservation.validateStoreOwnership(manager);
        reservationDao.delete(reservationId);
    }

    @Transactional
    public void deleteReservationWait(Long reservationId, Long memberId) {
        reservationWaitDao.deleteByReservationIdAndMemberId(reservationId, memberId);
    }
}
