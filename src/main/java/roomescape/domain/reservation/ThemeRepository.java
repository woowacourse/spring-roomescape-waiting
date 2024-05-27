package roomescape.domain.reservation;

import java.util.List;

public interface ThemeRepository {

    Theme save(Theme theme);

    List<Theme> findAll();

    List<Theme> findPopularThemesByFilter(PopularThemeLookupFilter filter);

    Theme getById(long id);

    void deleteById(long id);
}
