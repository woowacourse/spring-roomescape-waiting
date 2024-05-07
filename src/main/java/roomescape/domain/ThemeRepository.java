package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    Theme save(Theme theme);

    List<Theme> findAllThemes();

    Optional<Theme> findById(Long id);

    List<Theme> findTopReservedThemesByDateRangeAndLimit(LocalDate start, LocalDate end, int limit);

    void delete(Long id);
}
