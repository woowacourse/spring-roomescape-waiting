package roomescape.theme.domain.repository;

import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;

public interface ThemeRepository {
    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    Theme save(Theme theme);

    Integer delete(long id);

    Boolean existsByNameAndDescription(Theme theme);
}
