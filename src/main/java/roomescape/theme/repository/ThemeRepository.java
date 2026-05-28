package roomescape.theme.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;

public interface ThemeRepository {
    Theme save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    List<Theme> findTopThemesByReservationCount(LocalDate startDate, LocalDate endDate, int limit);

    boolean existsById(Long id);

    boolean cancelById(Long id, LocalDateTime now);
}
