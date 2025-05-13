package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Theme;

public interface ThemeRepository {
    Long save(Theme theme);

    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    boolean deleteById(Long id);

    List<Theme> findThemeRanking(int count, LocalDate startDate, LocalDate endDate);
}
