package roomescape.schedule.respository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.schedule.domain.Schedule;

import java.time.LocalDate;
import java.util.Optional;

public interface JpaScheduleRepository extends JpaRepository<Schedule, Long> {
    Optional<Schedule> findByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
}
