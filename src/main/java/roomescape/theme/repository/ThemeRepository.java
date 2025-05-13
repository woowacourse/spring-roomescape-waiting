package roomescape.theme.repository;

import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;

public interface ThemeRepository {

    List<Theme> findAll();

    Optional<Theme> findById(ThemeId id);

    Theme save(Theme theme);

    void deleteById(ThemeId id);
}
