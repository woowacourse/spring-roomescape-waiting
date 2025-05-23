package roomescape.schedule.respository;

import roomescape.schedule.domain.Schedule;

import java.time.LocalDate;
import java.util.Optional;

public interface ScheduleRepository {
    Schedule save(Schedule schedule);

    Optional<Schedule> findById(Long id);

    Optional<Schedule> findByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);
}
