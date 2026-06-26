package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Theme;
import roomescape.service.dto.PopularTheme;

public interface ThemeRepository {
    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    Theme save(Theme theme);

    void deleteById(Long id);

    List<PopularTheme> findPopular();

    boolean existsByName(String name);

    boolean existsById(Long id);
}
