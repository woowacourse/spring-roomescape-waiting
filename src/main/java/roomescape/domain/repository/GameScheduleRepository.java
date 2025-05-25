package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.entity.GameSchedule;

public interface GameScheduleRepository extends JpaRepository<GameSchedule, Long> {

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    Optional<GameSchedule> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
} 
