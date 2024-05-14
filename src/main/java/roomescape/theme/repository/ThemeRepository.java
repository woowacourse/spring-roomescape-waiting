package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Name;
import roomescape.theme.domain.Theme;

public interface ThemeRepository {

    Theme save(Theme theme);

    void delete(Long id);

    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    List<Theme> findTrendings(LocalDate start, LocalDate end, Long limit);

    boolean hasTheme(Name name);
}
