package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    Long create(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    void deleteById(Long id);

    List<Theme> findRankByDate(LocalDate startDate, LocalDate endDate, int limit);
}
