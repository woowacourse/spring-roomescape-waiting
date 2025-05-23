package roomescape.domain.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.entity.GameSchedule;

public interface GameScheduleRepository extends JpaRepository<GameSchedule, Long> {
    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
} 
