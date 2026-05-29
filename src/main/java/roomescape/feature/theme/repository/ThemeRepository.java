package roomescape.feature.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.feature.theme.domain.Theme;

public interface ThemeRepository {

    List<Theme> findAllByDeletedAtIsNull();

    Theme save(Theme theme);

    void deleteThemeById(Long id);

    Optional<Theme> findThemeByIdAndDeletedAtIsNull(Long id);

    boolean existsThemeByIdAndDeletedAtIsNull(Long id);

    boolean existsThemeByNameAndDeletedAtIsNull(String name);

    List<Theme> findPopularThemesDateBetween(LocalDate startDate, LocalDate endDate, Integer limit);
}
