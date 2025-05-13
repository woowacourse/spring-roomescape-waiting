package roomescape.business.model.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.vo.Id;

public interface ThemeRepository {

    void save(Theme theme);

    List<Theme> findAll();

    List<Theme> findPopularThemes(LocalDate startInclusive, LocalDate endInclusive, int count);

    Optional<Theme> findById(Id themeId);

    boolean existById(Id themeId);

    void deleteById(Id themeId);
}
