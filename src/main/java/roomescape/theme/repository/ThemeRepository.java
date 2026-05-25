package roomescape.theme.repository;

import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;

public interface ThemeRepository {

    Theme save(Theme theme);

    Optional<Theme> findById(Long id);

    boolean existByName(String name);

    List<Theme> findAll();

    int deleteById(Long id);
}
