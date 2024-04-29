package roomescape.theme.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    List<Theme> findByPeriodOrderByReservationCount(LocalDate start, LocalDate end, int count);

    Theme save(Theme theme);

    void deleteById(Long id);
}
