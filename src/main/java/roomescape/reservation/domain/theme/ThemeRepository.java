package roomescape.reservation.domain.theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    boolean existsByThemeName(ThemeName themeName);

    Theme save(Theme theme);

    void deleteById(long id);

    List<Theme> findAll();

    Optional<Theme> findById(long id);

    List<Theme> findPopularThemes(LocalDate from, LocalDate to, int count);
}
