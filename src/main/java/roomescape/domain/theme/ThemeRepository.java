package roomescape.domain.theme;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    Theme save(Theme theme);

    int deleteById(Long id);
}
