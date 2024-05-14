package roomescape.domain.repository;

import java.util.List;
import java.util.Optional;

import roomescape.domain.Theme;

public interface ThemeRepository {
    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    List<Theme> findThemesByPeriodWithLimit(String startDate, String endDate, int limit);

    Theme save(Theme theme);

    void delete(Theme theme);

    void deleteAll();
}
