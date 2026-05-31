package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Schedule;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.repository.ScheduleDao;

import java.time.LocalDate;

@Service
public class ScheduleService {

    private final ScheduleDao scheduleDao;

    public ScheduleService(ScheduleDao scheduleDao) {
        this.scheduleDao = scheduleDao;
    }

    public Schedule getOrCreateSchedule(
            LocalDate date,
            Long timeId,
            Long themeId
    ) {
        return scheduleDao.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseGet(() -> {
                    Long scheduleId = scheduleDao.save(date, timeId, themeId);
                    return getById(scheduleId);
                });
    }

    public Schedule getById(Long id) {
        return scheduleDao.findById(id).orElseThrow(()
                -> new RoomescapeException(DomainErrorCode.NOT_FOUND_RESERVATION, "해당 ID의 스케줄이 존재하지 않습니다. ID: " + id)
        );
    }
}
