package roomescape.theme.domain;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    boolean existsById(Long id);

    boolean existsByName(ThemeName name);

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    Theme save(Theme theme);

    void deleteById(Long id);
}
