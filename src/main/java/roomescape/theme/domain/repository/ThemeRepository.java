package roomescape.theme.domain.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;

public interface ThemeRepository {
    boolean existsByName(String name);

    Collection<Theme> findAll();

    List<Theme> findRankedByPeriod(LocalDate from, LocalDate to, int limit);

    Theme save(Theme theme);

    void deleteById(Long id);

    Optional<Theme> findById(Long id);

    boolean existsById(Long id);
}
