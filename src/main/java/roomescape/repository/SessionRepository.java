package roomescape.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Session;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByDateAndTimeSlotIdAndThemeId(LocalDate date, Long timeSlotId, Long themeId);
}
