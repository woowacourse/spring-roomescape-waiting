package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Theme;

public interface ThemeRepository {

    Theme save(Theme theme);

    void delete(Long id);

    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    List<Theme> findPopularThemes(LocalDate startDate, LocalDate endDate, Long limit);

    boolean existByThemeName(String name);
}
