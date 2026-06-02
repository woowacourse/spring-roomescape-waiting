package roomescape.theme.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;

public interface ThemeRepository {
    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    List<PopularTheme> findTop10PopularThemesBetween(LocalDate from, LocalDate to);

    Theme save(Theme theme);

    Integer delete(long id);

    Boolean existsByNameAndDescription(Theme theme);
}
