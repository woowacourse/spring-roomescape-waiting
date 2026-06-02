package roomescape.schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository {
    Schedule save(Schedule schedule);

    Optional<Long> findScheduleIdByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    Optional<Schedule> findById(long id);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    List<Schedule> findAll();

    void deleteById(long id);

    boolean existsAlreadySchedule(LocalDate date, long themeId, long timeId);
}
