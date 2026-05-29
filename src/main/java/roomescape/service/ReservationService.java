package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.*;
import roomescape.domain.ReservationTime;
import roomescape.dto.AdminReservationResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.repository.ReservationDao;
import roomescape.repository.ScheduleDao;
import roomescape.repository.ReservationTimeDao;

@Service
@Transactional(readOnly = true)
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
            throw new RoomescapeException(DomainErrorCode.DUPLICATE_RESERVATION, "이미 예약중인 시간입니다.");
        }

        try {
            Long scheduleId = getOrCreateScheduleId(request).get();
            Schedule schedule = scheduleDao.findById(scheduleId);

            Long reservationId = reservationDao.save(request.name(), scheduleId, now);
            Reservation reservation = reservationDao.findById(reservationId).get();
            return ReservationResponse.from(reservation, schedule,
                    reservationDao.findOrderByReservationId(reservationId, scheduleId));
        } catch (DuplicateKeyException e) {
            throw new RoomescapeException(DomainErrorCode.DUPLICATE_RESERVATION, "해당 시간은 이미 예약이 마감되었습니다. 다른 시간을 선택해주세요.");
        }
    }

    @Transactional
    public void update(Long reservationId, LocalDateTime now, ReservationRequest request) {
        try {
            ReservationTime reservationTime = reservationTimeDao.findById(request.timeId());
            LocalDateTime targetDateTime = LocalDateTime.of(request.date(), reservationTime.getStartAt());
            validateDateAndTimeNotPast(now, targetDateTime);

            Reservation reservation = reservationDao.findById(reservationId).get();
            Schedule currentSchedule = scheduleDao.findById(reservation.getScheduleId());
            Long newScheduleId = getOrCreateScheduleId(request).get();
            Schedule newSchedule = scheduleDao.findById(newScheduleId);
            validateSameTheme(currentSchedule.getTheme(), newSchedule.getTheme());

            if (reservationDao.existByNameScheduleIdStatus(request.name(), newScheduleId, Status.RESERVED)) {
                throw new RoomescapeException(DomainErrorCode.DUPLICATE_RESERVATION, "이미 예약중인 시간입니다.");
            }

            reservationDao.update(reservation.getId(), newSchedule.getId());
        } catch (DuplicateKeyException e) {
            throw new RoomescapeException(DomainErrorCode.DUPLICATE_RESERVATION, "해당 시간은 이미 예약이 마감되었습니다. 다른 시간을 선택해주세요.");
        }
    }

    @Transactional
    public void cancel(LocalDateTime now, Long reservationId) {
        Reservation reservation = reservationDao.findById(reservationId).get();
        Schedule schedule = scheduleDao.findById(reservation.getScheduleId());
        LocalDateTime scheduledAt = LocalDateTime.of(schedule.getDate(), schedule.getTime().getStartAt());
        if (now.isAfter(scheduledAt)) {
            throw new RoomescapeException(DomainErrorCode.PAST_RESERVATION, "이미 지난 예약은 취소할 수 없습니다.");
        }
        reservationDao.delete(reservation.getId(), Status.CANCELED);
    }

    public List<AdminReservationResponse> getAllReservations() {
        return scheduleDao.findAll().stream()
                .map(s -> AdminReservationResponse.from(s, s.getTheme()))
                .toList();
    }

    public List<ReservationResponse> findAllByName(String username) {
        return reservationDao.findByUserName(username);
    }

    private void validateDateAndTimeNotPast(LocalDateTime now, LocalDateTime reservationTime) {
        if (now.isAfter(reservationTime)) {
            throw new RoomescapeException(DomainErrorCode.PAST_RESERVATION, "지난 날짜/시간으로 예약할 수 없습니다.");
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
            throw new RoomescapeException(DomainErrorCode.DUPLICATE_RESERVATION, "해당 시간은 이미 예약이 마감되었습니다. 다른 시간을 선택해주세요.");
        }
    }

    private void validateSameTheme(Theme currentTheme, Theme newTheme) {
        if (!currentTheme.equals(newTheme)) {
            throw new RoomescapeException(DomainErrorCode.INVALID_INPUT, "예약의 테마는 변경할 수 없습니다. 예약 취소 후 다시 예약해주세요.");
        }
    }

    public Reservation getById(Long id) {
        return reservationDao.findById(id)
                .orElseThrow(() -> new RoomescapeException(DomainErrorCode.NOT_FOUND_RESERVATION,
                        "해당 ID의 예약이 존재하지 않습니다. ID: " + id));
    }
}
