package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;

public interface ThemeRepository {

    Theme save(Theme theme);

    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    List<Theme> findPopularThemes(LocalDate startDate, LocalDate endDate, Long limit);

    void delete(Long id);

    boolean existByThemeName(String name);
}
