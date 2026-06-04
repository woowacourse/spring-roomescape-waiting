package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Wait;
import roomescape.repository.dto.WaitDetailDto;

public interface WaitRepository {

    Wait save(Wait waitWithoutId);

    Optional<WaitDetailDto> findById(Long id);

    List<WaitDetailDto> findBySlot(LocalDate reservationDate, Long timeId, Long themeId);

    List<WaitDetailDto> findByName(String name);

    List<WaitDetailDto> findAll();

    Long findOrderByWait(Wait wait);

    void deleteById(Long id);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);
}
