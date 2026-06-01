package roomescape.theme.domain;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    Theme save(Theme theme);

    Optional<Theme> findById(Long id);

    boolean existsByName(String name);

    List<Theme> findAll();

    void delete(Theme theme);
}
