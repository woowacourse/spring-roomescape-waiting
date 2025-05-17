package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;

public interface ThemeRepository {

    boolean existsByName(ThemeName name);

    Theme save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(long id);

    void deleteById(long id);

    List<Theme> findPopularThemes(LocalDate from, LocalDate to, int count);
}
