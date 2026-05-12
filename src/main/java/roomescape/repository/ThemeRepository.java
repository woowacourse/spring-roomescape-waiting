package roomescape.repository;

import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    List<Theme> findAll();

    Theme save(Theme theme);

    void deleteById(Long id);

    Optional<Theme> findById(Long id);

    List<Theme> getPopularThemes(LocalDate start, LocalDate end, Integer limit);
}
