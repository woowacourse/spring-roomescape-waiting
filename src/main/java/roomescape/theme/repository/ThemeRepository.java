package roomescape.theme.repository;

import roomescape.theme.domain.Theme;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    Theme save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    int deleteById(Long id);

    Boolean existsByName(String name);
}
