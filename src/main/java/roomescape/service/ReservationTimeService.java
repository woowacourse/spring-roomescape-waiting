package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationStatus;
import roomescape.controller.dto.ReservationTimeRequest;
import roomescape.domain.ReservationTime;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.repository.ReservationTimeDao;
import roomescape.repository.ScheduleDao;
import roomescape.service.dto.AvailableTimeResult;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimeDao reservationTimeDao;
    private final ScheduleDao scheduleDao;

    public ReservationTimeService(ReservationTimeDao reservationTimeDao, ScheduleDao scheduleDao) {
        this.reservationTimeDao = reservationTimeDao;
        this.scheduleDao = scheduleDao;
    }

    @Transactional
    public Long saveReservationTime(ReservationTimeRequest request) {
        validateDuplicateTime(request.startAt());

        try {
            return reservationTimeDao.save(request.startAt());
        } catch (DuplicateKeyException e) {
            throw new RoomescapeException(DomainErrorCode.DUPLICATE_RESERVATION_TIME, "이미 존재하는 시간은 저장할 수 없습니다.");
        }
    }

    @Transactional
    public void deleteReservationTime(long id) {
        if (scheduleDao.existsByTimeId(id)) {
            throw new RoomescapeException(DomainErrorCode.REFERENTIAL_INTEGRITY,
                    "이 시간을 참조하는 예약이 있어 삭제할 수 없습니다. ID: " + id);
        }

        try {
            reservationTimeDao.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new RoomescapeException(DomainErrorCode.REFERENTIAL_INTEGRITY, "예약중인 시간은 삭제할 수 없습니다.");
        }
    }

    public List<ReservationTime> findAll() {
        return reservationTimeDao.findAll();
    }

    public List<AvailableTimeResult> findAvailableTimes(long themeId, LocalDate date) {
        return reservationTimeDao.findAvailableTimes(
                themeId,
                date,
                ReservationStatus.CANCELED
        );
    }

    private void validateDuplicateTime(LocalTime startAt) {
        if (reservationTimeDao.existsByStartAt(startAt)) {
            throw new RoomescapeException(DomainErrorCode.DUPLICATE_RESERVATION_TIME, "해당 시간이 이미 존재합니다.");
        }
    }
}
