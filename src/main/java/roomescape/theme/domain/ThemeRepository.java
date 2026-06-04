package roomescape.theme.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.ThemeRankResult;

public interface ThemeRepository {

    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    List<ThemeRankResult> findPopularThemes(int rankLimit, LocalDate startDay, LocalDate today);

    Theme save(Theme theme);

    int deleteById(Long id);
}
