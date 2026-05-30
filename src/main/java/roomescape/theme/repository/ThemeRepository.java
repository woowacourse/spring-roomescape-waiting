package roomescape.theme.repository;

import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    Theme save(Theme theme);

    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    boolean existsReservationByThemeId(Long themeId);

    void deleteById(Long id);

    List<Long> findTopThemeIds(LocalDate startDate, LocalDate endDate, int limit);

    List<Theme> findAllByIds(List<Long> ids);
}
