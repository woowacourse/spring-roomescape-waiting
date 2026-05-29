package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Wait;

public interface WaitRepository {

    Wait save(Wait waitWithoutId);

    Optional<Wait> findById(Long id);

    List<Wait> findBySlot(LocalDate reservationDate, Long timeId, Long themeId);

    List<Wait> findByName(String name);

    List<Wait> findAll();

    Long findOrderByWait(Wait wait);

    void delete(Long id);
}
