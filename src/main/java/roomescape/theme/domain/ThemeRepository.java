package roomescape.theme.domain;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository {

    Theme save(Theme theme);

    void deleteById(Long themeId);

    boolean existsByName(String name);

    Theme getById(Long themeId);

    List<Theme> findAll();

    List<Theme> findTopNThemesByReservationCountInDateRange(LocalDate dateFrom, LocalDate dateTo, int limit);
}
