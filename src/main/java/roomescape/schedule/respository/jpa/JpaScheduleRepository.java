package roomescape.schedule.respository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.schedule.domain.Schedule;

public interface JpaScheduleRepository extends JpaRepository<Schedule, Long> {
}
