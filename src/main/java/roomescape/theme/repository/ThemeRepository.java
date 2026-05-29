package roomescape.theme.repository;

import java.util.List;
import java.util.Optional;
import roomescape.theme.Theme;

public interface ThemeRepository {

    List<Theme> findAll();

    Optional<Theme> findById(long id);

    int deleteById(long id);

    Theme save(Theme theme);

    boolean existsByName(String name);

    List<Theme> findPopularThemes(int period, int limit);

}
