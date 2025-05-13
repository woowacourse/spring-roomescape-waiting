package roomescape.repository.theme;

import java.util.List;
import java.util.Optional;
import roomescape.domain.theme.Theme;


public interface ThemeRepository {
    long add(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    void deleteById(Long id);
}
