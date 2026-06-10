package roomescape.repository;

import java.time.LocalDate;
import java.util.Optional;
import roomescape.domain.Wait;
import roomescape.domain.Waits;

public interface WaitRepository {

    Wait save(Wait waitWithoutId);

    Optional<Wait> findById(Long id);

    Waits findBySlot(LocalDate reservationDate, Long timeId, Long themeId);

    Waits findByName(String name);

    Waits findAll();

    Long findOrderByWait(Wait wait);

    void deleteById(Long id);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);
}
