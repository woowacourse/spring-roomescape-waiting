package roomescape.theme.repository;

import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;

public interface ThemeRepository {

    Theme save(Theme theme);

    void deleteById(Long themeId);

    List<Theme> findAll();

    Optional<Theme> findById(Long id);
}
