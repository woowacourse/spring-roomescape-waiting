package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Theme;

public interface ThemeRepository {

    Theme save(Theme themeWithoutId);

    Optional<Theme> findById(Long id);

    List<Theme> findRanking(LocalDate startDate, LocalDate endDate, int limit);

    List<Theme> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);
}
