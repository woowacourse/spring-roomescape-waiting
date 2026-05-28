package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Theme;
import roomescape.domain.repository.projection.PopularThemeProjection;

public interface ThemeRepository {
    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    Theme save(Theme theme);

    void deleteById(Long id);

    List<PopularThemeProjection> findPopularBetween(LocalDate from, LocalDate to, int limit);
}
