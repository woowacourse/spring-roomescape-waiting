package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.Time;
import roomescape.dto.ReservationResponse;
import roomescape.dto.ReservationUpdateRequest;
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

    public ReservationResponse save(LocalDateTime now, ReservationRequest request) {
        Time reservationTime = timeDao.findById(request.timeId());
        LocalDateTime time = LocalDateTime.of(request.date(), reservationTime.getStartAt());
        validateDateAndTimeNotPast(now,time);

        try{
            Long reservationId = reservationSlotDao.save(request.date(), request.timeId(), request.themeId());
            ReservationSlot reservationSlot = reservationSlotDao.findById(reservationId);
            Long waitingId = reservationDao.save(request.name(), reservationId);
            Reservation reservation = reservationDao.findById(waitingId);
            return ReservationResponse.from(reservationSlot, WaitingResponse.from(reservation, reservationDao.findOrderByReservationId(waitingId, reservationId)));
        } catch (DuplicateKeyException e){
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    public void update(Long waitingId, LocalDateTime now, ReservationUpdateRequest request) {
        try {
            Time time = timeDao.findById(request.timeId());
            LocalDateTime targetDateTime = LocalDateTime.of(request.targetDate(), time.getStartAt());
            validateDateAndTimeNotPast(now, targetDateTime);
            Reservation reservation = reservationDao.findById(waitingId);
            reservationDao.delete(waitingId);

            if (!reservationDao.existByReservationId(reservation.getReservationId())){
                reservationSlotDao.delete(reservation.getReservationId());
            }

            ReservationSlot reservationSlot = reservationSlotDao.findById(reservation.getReservationId());
            Optional<Long> reservationId = reservationSlotDao.findIdByDateAndTimeIdAndThemeId(request.targetDate(), request.timeId(), reservationSlot.getTheme().getId());
            if (reservationId.isEmpty()) {
                reservationId = Optional.of(reservationSlotDao.save(request.targetDate(), request.timeId(), reservationSlot.getTheme().getId()));
            }
            reservationDao.save(request.name(), reservationId.get());
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    public void delete(LocalDateTime now, Long id) {
        ReservationSlot reservationSlot = reservationSlotDao.findById(id);
        LocalDateTime localDateTime = LocalDateTime.of(reservationSlot.getDate(), reservationSlot.getTime().getStartAt());
        if (now.isAfter(localDateTime )) {
            throw new CustomException(ErrorCode.UNALLOWED_DELETE_PAST_RESERVATION);
        }
        reservationSlotDao.delete(id);
    }

    public List<ReservationResponse> findAllByName(String username) {
        return reservationSlotDao.findByUserName(username);
    }

    private void validateDateAndTimeNotPast(LocalDateTime now, LocalDateTime reservationTime) {
        if (now.isAfter(reservationTime)) {
            throw new CustomException(ErrorCode.PAST_DATE_RESERVATION);
        }
    }
}
