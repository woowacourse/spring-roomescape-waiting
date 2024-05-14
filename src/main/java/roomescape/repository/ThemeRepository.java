package roomescape.repository;

import java.util.Optional;
import roomescape.domain.Theme;
import roomescape.domain.Themes;

public interface ThemeRepository {
    Themes findAll();

    Optional<Theme> findById(long id);

    Theme save(Theme theme);

    void delete(long id);
}
