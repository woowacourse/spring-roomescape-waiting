package roomescape.theme.repository;

import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    List<Theme> findAll();

    Optional<Theme> findById(Long themeId);

    Theme save(Theme themeWithoutId);

    boolean deleteById(Long themeId);

    List<Theme> findPopularThemes(LocalDate startDate, LocalDate today);
}
