package roomescape.theme.repository;

import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {
    Theme save(Theme theme);

    List<Theme> findAll();
    Optional<Theme> findById(Long id);
    List<Theme> findTopThemesByReservationCount(LocalDate startDate, LocalDate endDate, int limit);

    boolean existsById(Long id);

    boolean cancelById(Long id);
}
