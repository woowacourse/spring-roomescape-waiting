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
import roomescape.dto.ReservationResponse;
import roomescape.dto.WaitingResponse;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationSlotDao;
import roomescape.dto.ReservationRequest;
import roomescape.repository.TimeDao;
import roomescape.repository.ReservationDao;

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
        validateDateAndTimeNotPast(now,time);

        try{
            Long reservationSlotId = getOrCreateReservationSlotId(request).get();
            ReservationSlot reservationSlot = reservationSlotDao.findById(reservationSlotId);

            Long reservationId = reservationDao.save(request.name(), reservationSlotId, now);
            Reservation reservation = reservationDao.findById(reservationId);
            return ReservationResponse.from(reservation, reservationSlot, reservationDao.findOrderByReservationId(reservationId, reservationSlotId));
        } catch (DuplicateKeyException e){
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
            Optional<Long> newReservationSlotId = getOrCreateReservationSlotId(request);
            ReservationSlot newReservationSlot = reservationSlotDao.findById(newReservationSlotId.get());
            validateSameTheme(reservationSlot.getTheme(), newReservationSlot.getTheme());

            reservationDao.update(reservation.getId(), newReservationSlot.getId());
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    public void delete(LocalDateTime now, Long reservationId) {
        Reservation reservation = reservationDao.findById(reservationId);
        ReservationSlot reservationSlot = reservationSlotDao.findById(reservation.getReservationSlotId());
        LocalDateTime localDateTime = LocalDateTime.of(reservationSlot.getDate(), reservationSlot.getTime().getStartAt());
        if (now.isAfter(localDateTime )) {
            throw new CustomException(ErrorCode.UNALLOWED_DELETE_PAST_RESERVATION);
        }
        reservationDao.delete(reservation.getId(), Status.CANCELLED);
    }

    public List<ReservationResponse> findAllByName(String username) {
        return reservationSlotDao.findByUserName(username);
    }

    private void validateDateAndTimeNotPast(LocalDateTime now, LocalDateTime reservationTime) {
        if (now.isAfter(reservationTime)) {
            throw new CustomException(ErrorCode.PAST_DATE_RESERVATION);
        }
    }

    private Optional<Long> getOrCreateReservationSlotId(ReservationRequest request) {
        try{
            Optional<Long> reservationSlotId = reservationSlotDao.findIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId());
            if (reservationSlotId.isEmpty()) {
                reservationSlotId = Optional.of(reservationSlotDao.save(request.date(), request.timeId(), request.themeId()));
            }
            return reservationSlotId;
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private void validateSameTheme(Theme reservationTheme, Theme newReservationTheme) {
        if (!reservationTheme.equals(newReservationTheme)) {
            throw new CustomException(ErrorCode.UNALLOWED_CHANGE_RESERVATION_THEME);
        }
    }
}
