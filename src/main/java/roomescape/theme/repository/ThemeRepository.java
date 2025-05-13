package roomescape.theme.repository;

import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;

public interface ThemeRepository {
    Long save(Theme theme);

    void deleteById(Long id);

    boolean existsByName(String name);

    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    List<Theme> findTop10ThemesByReservationCountWithin7Days(int days, int limit);
}
