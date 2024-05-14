package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Theme;

public interface ThemeRepository {
    List<Theme> findAll();

    Theme save(Theme theme);

    void deleteById(long themeId);

    Optional<Theme> findById(long themeId);

    List<Theme> findPopularThemes(LocalDate startDate, LocalDate endDate, int limit);
}
