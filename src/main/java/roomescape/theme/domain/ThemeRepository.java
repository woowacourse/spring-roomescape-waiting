package roomescape.theme.domain;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    boolean existsById(ThemeId id);

    boolean existsByName(ThemeName name);

    List<Theme> findAll();

    Optional<Theme> findById(ThemeId id);

    Theme save(Theme theme);

    void deleteById(ThemeId id);
}
