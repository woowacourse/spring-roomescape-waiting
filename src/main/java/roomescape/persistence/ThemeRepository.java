package roomescape.persistence;

import java.util.Optional;
import roomescape.domain.Theme;

public interface ThemeRepository {

    boolean isActiveByName(String name);

    Theme save(Theme theme);

    void update(Theme theme);

    Optional<Theme> findById(long id);

}
