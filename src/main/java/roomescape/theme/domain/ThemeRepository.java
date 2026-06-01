package roomescape.theme.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.common.exception.NotFoundException;

public interface ThemeRepository {
    Theme save(Theme theme);

    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    List<Theme> findAll(int page, int size);

    List<Theme> findByReservationCountWithLimit(LocalDate startDate, LocalDate endDate, int limit);

    boolean existsByName(String name);

    void update(Theme theme);

    default Theme getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));
    }
}
