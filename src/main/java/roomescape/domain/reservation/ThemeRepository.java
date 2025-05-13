package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    Long create(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    void deleteById(Long id);

    List<Theme> findRankBetweenDate(LocalDate startDate, LocalDate endDate, int limit);
}
