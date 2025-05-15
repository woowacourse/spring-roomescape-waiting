package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Theme;

public interface ThemeRepository {

    Theme save(Theme theme);

    List<Theme> findAll();

    List<Theme> findTopThemes(LocalDate from, LocalDate to, int limit);

    Optional<Theme> findById(Long id);

    void deleteById(Long id);
}
