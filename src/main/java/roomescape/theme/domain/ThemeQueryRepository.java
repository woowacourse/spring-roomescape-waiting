package roomescape.theme.domain;

import java.time.LocalDate;
import java.util.List;

public interface ThemeQueryRepository {

    boolean existsByName(String name);

    Theme getByIdOrThrow(Long id);

    List<Theme> findAll();

    List<Theme> findTopNThemesByReservationCountInDateRange(LocalDate dateFrom, LocalDate dateTo, int limit);
}
