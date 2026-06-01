package roomescape.feature.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.feature.theme.domain.Theme;

public interface ThemeRepository {

    List<Theme> findAllByNotDeleted();

    Theme save(Theme theme);

    void deleteThemeById(Long id);

    Optional<Theme> findThemeByIdAndNotDeleted(Long id);

    boolean existsThemeByIdAndNotDeleted(Long id);

    boolean existsThemeByNameAndNotDeleted(String name);

    List<Theme> findPopularThemesDateBetween(LocalDate startDate, LocalDate endDate, Integer limit);
}
