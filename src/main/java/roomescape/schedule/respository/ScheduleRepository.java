package roomescape.schedule.respository;

import roomescape.schedule.domain.Schedule;

import java.util.Optional;

public interface ScheduleRepository {
    Schedule save(Schedule schedule);

    Optional<Schedule> findById(Long id);
}
