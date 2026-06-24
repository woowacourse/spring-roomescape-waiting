package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    Theme save(Theme theme);

    Optional<Theme> findById(Long id);

    List<Theme> findPopularThemes(LocalDate start, LocalDate end, Integer limit);

    List<Theme> findAll();

    void deleteById(Long id);
}
