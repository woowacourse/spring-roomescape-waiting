package roomescape.service;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.exception.PastReservationCancelNotAllowedException;
import roomescape.exception.PastReservationNotAllowedException;
import roomescape.exception.ReservationAlreadyExistsException;
import roomescape.exception.ReservationNotFoundException;
import roomescape.exception.ReservationOwnerMismatchException;
import roomescape.exception.ReservationTimeNotFoundException;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;

    public ReservationService(ReservationDao reservationDao, ReservationTimeDao reservationTimeDao) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
    }

    public List<Reservation> getReservations(Long memberId) {
        return reservationDao.findAllReservationsByMemberId(memberId);
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
    public void deleteReservation(Long id, Long memberId) {
        Reservation reservation = findReservation(id);
        validateReservationOwner(memberId, reservation);
        validatePastReservationCancel(reservation.getDate(), reservation.getTime().getStartAt());
        reservationDao.delete(id);
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

    public List<Reservation> findByStoreId(Long storeId) {
        return reservationDao.findByStoreId(storeId);
    }

    @Transactional
    public void deleteByManager(Long reservationId, Member manager) {
        Reservation reservation = findReservation(reservationId);
        reservation.validateStoreOwnership(manager);
        reservationDao.delete(reservationId);
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
}
