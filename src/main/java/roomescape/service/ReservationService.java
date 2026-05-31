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
import roomescape.dto.CreatedWaitResult;
import roomescape.dto.WaitingResponseResult;
import roomescape.exception.reservation.*;
import roomescape.exception.reservationtime.ReservationTimeNotFoundException;
import roomescape.exception.reservationwait.PastReservationWaitNotAllowedException;
import roomescape.exception.reservationwait.ReservationWaitAlreadyExistsException;
import roomescape.exception.reservationwait.SelfReservationWaitNotAllowedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public List<Reservation> getReservations(Long memberId) {
        return reservationDao.findAllReservationsByMemberId(memberId);
    }

    public List<WaitingResponseResult> getWaitings(Long memberId) {
        return reservationWaitDao.findWaitingsByMemberId(memberId).stream()
                .map(WaitingResponseResult::from)
                .toList();
    }

    public List<Reservation> findByStoreId(Long storeId) {
        return reservationDao.findByStoreId(storeId);
    }

    @Transactional
    public Reservation createReservation(Long memberId, LocalDate date, Long timeId, Long themeId, Long storeId) {
        LocalTime startAt = reservationTimeDao.findReservationTimeById(timeId).getStartAt();
        validatePastReservationCreate(date, startAt);
        try {
            Long id = reservationDao.insertWithKeyHolder(memberId, date, timeId, themeId, storeId);
            return reservationDao.findReservationById(id);
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistsException();
        }
    }

    @Transactional
    public CreatedWaitResult createWait(Long memberId, Long reservationId) {
        Reservation reservation = findReservation(reservationId);
        validatePastReservationWaitCreate(reservation.getDate(), reservation.getTime().getStartAt());
        validateIfSelfReserved(memberId, reservation);
        try {
            Long waitId = reservationWaitDao.createReservationWait(memberId, reservationId);
            ReservationWait wait = reservationWaitDao.findReservationWaitById(waitId).get();
            Long order = reservationWaitDao.findWaitOrder(waitId);
            return new CreatedWaitResult(wait, order);
        } catch (DuplicateKeyException e) {
            throw new ReservationWaitAlreadyExistsException();
        }
    }

    @Transactional
    public Reservation updateReservation(Long id, LocalDate date, Long memberId, Long timeId) {
        ReservationTime reservationTime = findReservationTime(timeId);
        validateReservationOwner(memberId, findReservation(id));
        validatePastReservationCreate(date, reservationTime.getStartAt());
        try {
            reservationDao.updateById(id, date, timeId);
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistsException();
        }
        return reservationDao.findReservationById(id);
    }

    @Transactional
    public Reservation updateByManager(Long reservationId, LocalDate date, Long timeId, Member manager) {
        ReservationTime reservationTime = findReservationTime(timeId);
        Reservation reservation = findReservation(reservationId);
        reservation.validateStoreOwnership(manager);
        validatePastReservationCreate(date, reservationTime.getStartAt());
        try {
            reservationDao.updateById(reservationId, date, timeId);
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistsException();
        }
        return reservationDao.findReservationById(reservationId);
    }

    @Transactional
    public void deleteReservation(Long reservationId, Long memberId) {
        Reservation reservation = findReservation(reservationId);
        validateReservationOwner(memberId, reservation);
        validatePastReservationCancel(reservation.getDate(), reservation.getTime().getStartAt());
        reservationWaitDao.findEarliestMemberId(reservationId)
                .ifPresentOrElse(
                        m -> changeToRecentWaitingMember(reservationId, m),
                        () -> reservationDao.delete(reservationId));
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

    private void changeToRecentWaitingMember(Long reservationId, Long memberId) {
        reservationDao.updateMemberId(reservationId, memberId);
        reservationWaitDao.deleteByReservationIdAndMemberId(reservationId, memberId);
    }

    private void validateReservationOwner(Long memberId, Reservation reservation) {
        if (!reservation.getMemberId().equals(memberId)) {
            throw new ReservationOwnerMismatchException();
        }
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

    private void validatePastReservationCreate(LocalDate date, LocalTime startAt) {
        LocalDateTime present = LocalDateTime.now();
        LocalDateTime request = LocalDateTime.of(date, startAt);
        if (present.isAfter(request)) {
            throw new PastReservationNotAllowedException();
        }
    }

    private void validatePastReservationCancel(LocalDate date, LocalTime startAt) {
        LocalDateTime present = LocalDateTime.now();
        LocalDateTime request = LocalDateTime.of(date, startAt);
        if (present.isAfter(request)) {
            throw new PastReservationCancelNotAllowedException();
        }
    }

    private void validatePastReservationWaitCreate(LocalDate date, LocalTime startAt) {
        LocalDateTime present = LocalDateTime.now();
        LocalDateTime request = LocalDateTime.of(date, startAt);
        if (present.isAfter(request)) {
            throw new PastReservationWaitNotAllowedException();
        }
    }

    private static void validateIfSelfReserved(Long memberId, Reservation reservation) {
        if (reservation.getMemberId().equals(memberId)) {
            throw new SelfReservationWaitNotAllowedException();
        }
    }
}
