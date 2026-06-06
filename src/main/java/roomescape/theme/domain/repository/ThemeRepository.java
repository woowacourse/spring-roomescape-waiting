package roomescape.theme.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;

public interface ThemeRepository {
    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    List<Theme> findSortedPopularThemes(LocalDate from, LocalDate to, int limit);

    Theme save(Theme theme);

    void delete(long id);

    Boolean existsByNameAndDescription(Theme theme);
}
