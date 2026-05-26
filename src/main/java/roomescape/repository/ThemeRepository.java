package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Theme;

public interface ThemeRepository {

    Theme create(Theme themeWithoutId);

    Optional<Theme> read(Long id);

    List<Theme> readAll();

    void delete(Long id);

    List<Theme> readRanking(LocalDate startDate, LocalDate endDate, int limit);

    boolean existById(Long id);
}
