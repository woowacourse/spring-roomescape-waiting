package roomescape.domain;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    Theme create(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(long id);

    void deleteById(long id);
}
