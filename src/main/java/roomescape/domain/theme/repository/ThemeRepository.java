package roomescape.domain.theme.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.theme.domain.Theme;

public interface ThemeRepository {

    List<Theme> findAll();

    Theme insert(Theme theme);

    void deleteById(Long id);

    Optional<Theme> findById(Long id);
}
