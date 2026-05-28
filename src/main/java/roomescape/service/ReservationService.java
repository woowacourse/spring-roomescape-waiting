package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.*;
import roomescape.domain.ReservationTime;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationDao;
import roomescape.repository.ScheduleDao;
import roomescape.repository.ReservationTimeDao;

@Service
public class ReservationService {

    private final ScheduleDao scheduleDao;
    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;

    public ReservationService(ScheduleDao scheduleDao, ReservationDao reservationDao, ReservationTimeDao reservationTimeDao) {
        this.scheduleDao = scheduleDao;
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
    }

    @Transactional
    public ReservationResponse save(LocalDateTime now, ReservationRequest request) {
        ReservationTime reservationTime = reservationTimeDao.findById(request.timeId());
        LocalDateTime dateTime = LocalDateTime.of(request.date(), reservationTime.getStartAt());
        validateDateAndTimeNotPast(now, dateTime);

        if (reservationDao.existByNameScheduleIdStatus(
                request.name(),
                scheduleDao.findIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId()).orElse(null),
                Status.RESERVED)) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS_RESERVATION);
        }

        try {
            Long scheduleId = getOrCreateScheduleId(request).get();
            Schedule schedule = scheduleDao.findById(scheduleId);

            Long reservationId = reservationDao.save(request.name(), scheduleId, now);
            Reservation reservation = reservationDao.findById(reservationId);
            return ReservationResponse.from(reservation, schedule,
                    reservationDao.findOrderByReservationId(reservationId, scheduleId));
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    @Transactional
    public void update(Long reservationId, LocalDateTime now, ReservationRequest request) {
        try {
            ReservationTime reservationTime = reservationTimeDao.findById(request.timeId());
            LocalDateTime targetDateTime = LocalDateTime.of(request.date(), reservationTime.getStartAt());
            validateDateAndTimeNotPast(now, targetDateTime);

            Reservation reservation = reservationDao.findById(reservationId);
            Schedule currentSchedule = scheduleDao.findById(reservation.getScheduleId());
            Long newScheduleId = getOrCreateScheduleId(request).get();
            Schedule newSchedule = scheduleDao.findById(newScheduleId);
            validateSameTheme(currentSchedule.getTheme(), newSchedule.getTheme());

            if (reservationDao.existByNameScheduleIdStatus(request.name(), newScheduleId, Status.RESERVED)) {
                throw new CustomException(ErrorCode.ALREADY_EXISTS_RESERVATION);
            }

            reservationDao.update(reservation.getId(), newSchedule.getId());
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    public void delete(LocalDateTime now, Long reservationId, String username) {
        Reservation reservation = reservationDao.findById(reservationId);
        if (!reservation.getName().equals(username)) {
            throw new CustomException(ErrorCode.COMMON_UNAUTHORIZED);
        }
        Schedule schedule = scheduleDao.findById(reservation.getScheduleId());
        LocalDateTime scheduledAt = LocalDateTime.of(schedule.getDate(), schedule.getTime().getStartAt());
        if (now.isAfter(scheduledAt)) {
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

    private Optional<Long> getOrCreateScheduleId(ReservationRequest request) {
        try {
            Optional<Long> scheduleId = scheduleDao.findIdByDateAndTimeIdAndThemeId(
                    request.date(), request.timeId(), request.themeId());
            if (scheduleId.isEmpty()) {
                scheduleId = Optional.of(scheduleDao.save(request.date(), request.timeId(), request.themeId()));
            }
            return scheduleId;
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private void validateSameTheme(Theme currentTheme, Theme newTheme) {
        if (!currentTheme.equals(newTheme)) {
            throw new CustomException(ErrorCode.UNALLOWED_CHANGE_RESERVATION_THEME);
        }
    }
}
