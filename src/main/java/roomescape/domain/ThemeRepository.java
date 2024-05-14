package roomescape.domain;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    Theme save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    void deleteById(Long id);
}
