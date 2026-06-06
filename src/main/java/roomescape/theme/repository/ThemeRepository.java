package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;

public interface ThemeRepository {
    Theme save(Theme theme);
    Optional<Theme> findById(Long id);
    List<Theme> findAll();
    boolean hasReservation(Long themeId);
    void deleteById(Long id);
    List<Long> findTopThemeIds(LocalDate startDate, LocalDate endDate, int limit);
    List<Theme> findAllByIds(List<Long> ids);
}