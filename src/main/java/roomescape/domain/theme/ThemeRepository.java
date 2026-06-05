package roomescape.domain.theme;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface ThemeRepository {

    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    Theme save(Theme theme);

    int deleteById(Long id);

    List<Theme> findPopularThemes(int rankLimit, LocalDate startDay, LocalDate endDay);
}
