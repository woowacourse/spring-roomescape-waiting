package roomescape.theme.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    Theme save(Theme theme);

    Optional<Theme> findById(Long id);

    void deleteById(Long id);

    List<Theme> findPopularThemes(LocalDate start, LocalDate end, int limit);

    List<Theme> findAll();

    boolean existsById(Long id);
}
