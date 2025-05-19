package roomescape.theme.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    Theme save(Theme theme);

    void deleteById(Long id);

    boolean existsByName(String name);

    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    List<Theme> findTopNThemesByReservationCountInDateRange(LocalDate dateFrom, LocalDate dateTo, int limit);
}
