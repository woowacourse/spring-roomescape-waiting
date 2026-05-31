package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.Status;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationDao;
import roomescape.repository.ReservationSlotDao;
import roomescape.repository.TimeDao;

@Service
public class ReservationService {
    private final ReservationSlotDao reservationSlotDao;
    private final ReservationDao reservationDao;
    private final TimeDao timeDao;

    public ReservationService(ReservationSlotDao reservationSlotDao, ReservationDao reservationDao, TimeDao timeDao) {
        this.reservationSlotDao = reservationSlotDao;
        this.reservationDao = reservationDao;
        this.timeDao = timeDao;
    }

    @Transactional
    public ReservationResponse save(LocalDateTime now, ReservationRequest request) {
        Time reservationTime = timeDao.findById(request.timeId());
        LocalDateTime time = LocalDateTime.of(request.date(), reservationTime.getStartAt());
        validateDateAndTimeNotPast(now, time);

        try {
            Long reservationSlotId = getOrCreateReservationSlotId(request);
            ReservationSlot reservationSlot = reservationSlotDao.findById(reservationSlotId);
            validateSameReservation(request.name(), reservationSlotId);

            Long reservationId = reservationDao.save(request.name(), reservationSlotId, now);

            Reservation reservation = reservationDao.findById(reservationId);
            return ReservationResponse.from(reservation, reservationSlot, reservationDao.findOrderByReservationId(reservationId, reservationSlotId));
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    @Transactional
    public void update(Long reservationId, LocalDateTime now, ReservationRequest request) {
        try {
            Time time = timeDao.findById(request.timeId());
            LocalDateTime targetDateTime = LocalDateTime.of(request.date(), time.getStartAt());
            validateDateAndTimeNotPast(now, targetDateTime);

            Reservation reservation = reservationDao.findById(reservationId);
            ReservationSlot reservationSlot = reservationSlotDao.findById(reservation.getReservationSlotId());
            Long newReservationSlotId = getOrCreateReservationSlotId(request);
            validateSameReservation(request.name(), newReservationSlotId);

            ReservationSlot newReservationSlot = reservationSlotDao.findById(newReservationSlotId);
            validateSameTheme(reservationSlot.getTheme(), newReservationSlot.getTheme());

            reservationDao.update(reservation.getId(), newReservationSlot.getId());
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    public void delete(LocalDateTime now, Long reservationId, String name) {
        Reservation reservation = reservationDao.findById(reservationId);
        ReservationSlot reservationSlot = reservationSlotDao.findById(reservation.getReservationSlotId());
        validateReservationOwner(reservation, name);

        LocalDateTime localDateTime = LocalDateTime.of(reservationSlot.getDate(), reservationSlot.getTime().getStartAt());
        if (now.isAfter(localDateTime)) {
            throw new CustomException(ErrorCode.UNALLOWED_DELETE_PAST_RESERVATION);
        }
        reservationDao.delete(reservation.getId(), Status.CANCELED);
    }

    public List<ReservationResponse> findAllByName(String username) {
        return reservationDao.findByUserName(username);
    }

    private void validateDateAndTimeNotPast(LocalDateTime now, LocalDateTime reservationTime) {
        if (now.isAfter(reservationTime)) {
            throw new CustomException(ErrorCode.PAST_DATE_RESERVATION);
        }
    }

    private Long getOrCreateReservationSlotId(ReservationRequest request) {
        try {
            Optional<Long> reservationSlotId = reservationSlotDao.findIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId());
            return reservationSlotId.orElseGet(() -> reservationSlotDao.save(request.date(), request.timeId(), request.themeId()));
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private void validateSameTheme(Theme reservationTheme, Theme newReservationTheme) {
        if (!reservationTheme.equals(newReservationTheme)) {
            throw new CustomException(ErrorCode.UNALLOWED_CHANGE_RESERVATION_THEME);
        }
    }

    private void validateSameReservation(String name, Long reservationSlotId) {
        if (reservationDao.existByNameReservationIdStatus(name, reservationSlotId, Status.RESERVED)) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS_RESERVATION);
        }
    }

    private void validateReservationOwner(Reservation reservation, String name) {
        if (!reservation.getName().equals(name)) {
            throw new CustomException(ErrorCode.COMMON_UNAUTHORIZED);
        }
    }
}
