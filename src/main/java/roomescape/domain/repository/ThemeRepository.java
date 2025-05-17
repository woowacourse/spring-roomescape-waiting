package roomescape.domain.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Theme;

public interface ThemeRepository {
    Theme save(Theme theme);

    List<Theme> findAll();

    void deleteById(long id);

    Optional<Theme> findByName(String name);

    Optional<Theme> findById(Long id);
}