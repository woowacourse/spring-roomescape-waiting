package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.theme.domain.Theme;

public interface ThemeRepository {
    List<Theme> findAll();

    Theme findById(Long id);

    Theme save(Theme theme);

    boolean existsById(Long id);

    boolean deleteById(Long id);

    List<Theme> findBestThemesByDate(LocalDate startDate, LocalDate endDate, int limit);
}
