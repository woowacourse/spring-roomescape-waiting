package roomescape.theme.repository;

import roomescape.theme.domain.Theme;
import roomescape.theme.repository.projection.PopularThemeResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    List<Theme> findByIsActive(boolean isActive);

    List<PopularThemeResult> findPopularThemes(LocalDate startDate, LocalDate endDate, int limit);

    Theme save(Theme theme);

    boolean updateStatus(Theme theme);

    List<Theme> findSlogOfThemes();

}
