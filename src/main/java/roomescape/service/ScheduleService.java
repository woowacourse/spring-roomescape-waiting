package roomescape.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Schedule;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.repository.ScheduleDao;

import java.time.LocalDate;

import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@Service
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleDao scheduleDao;

    public ScheduleService(ScheduleDao scheduleDao) {
        this.scheduleDao = scheduleDao;
    }

    @Transactional(propagation = MANDATORY)
    public Schedule getOrCreateScheduleForUpdate(LocalDate date, Long timeId, Long themeId) {
        Schedule schedule = scheduleDao.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseGet(() -> createAndFind(date, timeId, themeId));
        lockById(schedule.getId());
        return schedule;
    }

    @Transactional(propagation = MANDATORY)
    public void lockById(Long id) {
        if (!scheduleDao.lockById(id)) {
            throw new RoomescapeException(DomainErrorCode.NOT_FOUND_SCHEDULE, "해당 ID의 스케줄이 존재하지 않습니다. ID: " + id);
        }
    }

    private Schedule createAndFind(
            LocalDate date,
            Long timeId,
            Long themeId
    ) {
        try {
            Long scheduleId = scheduleDao.save(date, timeId, themeId);
            return getById(scheduleId);
        } catch (DuplicateKeyException e) {
            return scheduleDao.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                    .orElseThrow(() -> new RoomescapeException(
                            DomainErrorCode.NOT_FOUND_SCHEDULE,
                            "동시에 생성된 스케줄을 조회할 수 없습니다."
                    ));
        }
    }

    public Schedule getById(Long id) {
        return scheduleDao.findById(id).orElseThrow(()
                -> new RoomescapeException(DomainErrorCode.NOT_FOUND_SCHEDULE, "해당 ID의 스케줄이 존재하지 않습니다. ID: " + id)
        );
    }
}
