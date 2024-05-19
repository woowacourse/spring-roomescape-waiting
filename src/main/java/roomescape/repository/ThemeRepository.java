package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Theme;

public interface ThemeRepository {
    List<Theme> findAll();

    List<Theme> findAndOrderByPopularity(LocalDate start, LocalDate end, int count);

    Optional<Theme> findById(long id);

    Theme save(Theme theme);

    void delete(long id);
}
