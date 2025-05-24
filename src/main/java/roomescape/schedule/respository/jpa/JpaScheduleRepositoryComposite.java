package roomescape.schedule.respository.jpa;

import org.springframework.stereotype.Repository;
import roomescape.schedule.domain.Schedule;
import roomescape.schedule.respository.ScheduleRepository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public class JpaScheduleRepositoryComposite implements ScheduleRepository {
    private final JpaScheduleRepository jpaScheduleRepository;

    public JpaScheduleRepositoryComposite(JpaScheduleRepository jpaScheduleRepository) {
        this.jpaScheduleRepository = jpaScheduleRepository;
    }

    @Override
    public Schedule save(Schedule schedule) {
        return jpaScheduleRepository.save(schedule);
    }

    @Override
    public Optional<Schedule> findById(Long id) {
        return jpaScheduleRepository.findById(id);
    }

    @Override
    public Optional<Schedule> findByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId) {
        return jpaScheduleRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return jpaScheduleRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }
}
