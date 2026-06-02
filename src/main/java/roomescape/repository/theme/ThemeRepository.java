package roomescape.repository.theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.theme.Theme;

public interface ThemeRepository {

    List<Theme> findAll();

    Optional<Theme> findById(long id);

    int deleteById(long id);

    Theme save(Theme theme);

    boolean existsByName(String name);

    List<Theme> findPopularThemes(LocalDate start, LocalDate end, int limit);

}
