package roomescape.domain.reservationdetail;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository {
    Theme save(Theme theme);

    Theme getById(Long id);

    Optional<Theme> findById(Long id);

    List<Theme> findThemesByPeriodWithLimit(String startDate, String endDate, int limit);

    List<Theme> findAll();

    void deleteById(Long themeId);
}
