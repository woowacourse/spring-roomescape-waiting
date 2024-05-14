package roomescape.core.repository;

import java.util.List;
import roomescape.core.domain.Theme;

public interface ThemeRepository {
    Long save(final Theme theme);

    List<Theme> findAll();

    Theme findById(final long id);

    Integer countByName(final String name);

    void deleteById(final long id);
}
