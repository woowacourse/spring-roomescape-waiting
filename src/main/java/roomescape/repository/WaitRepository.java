package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.Wait;

public interface WaitRepository {

    Wait create(Wait waitWithoutId);

    List<Wait> readBySlot(LocalDate reservationDate, Long timeId, Long themeId);

    List<Wait> readByName(String name);

    List<Wait> readAll();

    Long readOrderByWait(Wait wait);

    void delete(Long id);
}
