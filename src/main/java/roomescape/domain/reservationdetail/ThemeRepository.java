package roomescape.domain.reservationdetail;

import java.util.List;
import java.util.Optional;
import roomescape.exception.theme.NotFoundThemeException;

public interface ThemeRepository {
    Theme save(Theme theme);

    default Theme getById(Long id) {
        return findById(id)
                .orElseThrow(NotFoundThemeException::new);
    }

    Optional<Theme> findById(Long id);

    List<Theme> findThemesByPeriodWithLimit(String startDate, String endDate, int limit);

    List<Theme> findAll();

    void deleteById(Long themeId);
}
