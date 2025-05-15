package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    boolean existsByName(ThemeName name);

    Theme save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(long id);

    void deleteById(long id);

    List<Theme> findPopularThemes(LocalDate from, LocalDate to, int count);
}
