package roomescape.theme.repository;

import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {
    Theme save(Theme theme);

    List<Theme> findAll();

    List<Theme> findTopThemesByReservationCount(LocalDate startDate, LocalDate endDate, int limit);

    Optional<Theme> findById(Long id);

    boolean existsById(Long id);

    boolean existsByName(String name);

    void updateActive(Long id, boolean active);
}
