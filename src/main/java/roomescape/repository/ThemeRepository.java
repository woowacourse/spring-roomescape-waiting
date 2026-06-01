package roomescape.repository;

import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    List<Theme> findAll();

    Optional<Theme> findById(long themeId);

    List<Long> findReservedTimeIds(long themeId, LocalDate date);

    List<Theme> findPopularThemes(LocalDate startDate, LocalDate endDate, int limit);

    Theme save(Theme theme);

    void deleteById(long id);
}
