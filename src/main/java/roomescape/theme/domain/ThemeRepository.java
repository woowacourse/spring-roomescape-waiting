package roomescape.theme.domain;

import java.time.LocalDate;
import java.util.List;
import roomescape.theme.domain.exception.ThemeNotFoundException;
import java.util.Optional;

public interface ThemeRepository {
    Theme save(Theme theme);
    int delete(Theme theme);
    Optional<Theme> findById(Long id);
    List<Theme> findAll();
    List<Theme> findByReservationCountWithLimit(LocalDate startDate, LocalDate endDate, int limit);
    boolean existsByName(String name);

    default Theme getById(final Long id) {
        return findById(id)
                .orElseThrow(() -> new ThemeNotFoundException("존재하지 않은 테마입니다."));
    }
}
