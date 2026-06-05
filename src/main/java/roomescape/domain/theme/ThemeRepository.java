package roomescape.domain.theme;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    Optional<Theme> findThemeById(long id);

    List<Theme> findAllTheme();

    List<Theme> findAllByTopTheme();

    Long insert(Theme theme);

    void delete(Long id);
}
