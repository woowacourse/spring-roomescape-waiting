package roomescape.domain.theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.theme.ThemeRankResult;

public interface ThemeRepository {

    List<Theme> findAll();

    List<ThemeRankResult> findPopularThemes(int rankLimit, LocalDate startDay, LocalDate today);

    Theme save(Theme theme);

    int deleteById(Long id);
}
