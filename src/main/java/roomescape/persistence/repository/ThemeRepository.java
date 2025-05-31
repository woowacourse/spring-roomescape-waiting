package roomescape.persistence.repository;

import java.util.List;
import roomescape.model.Theme;
import roomescape.persistence.vo.Period;

public interface ThemeRepository {

    boolean isDuplicatedName(String name);

    List<Theme> findPopularThemesInPeriod(
            Period period,
            int size
    );

    Theme findById(Long id);

    List<Theme> findAll();

    Theme save(Theme theme);

    void deleteById(Long id);
}
