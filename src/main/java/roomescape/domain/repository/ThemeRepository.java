package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Theme;

public interface ThemeRepository {

    Optional<Theme> findById(long id);

    long save(Theme theme);

    boolean removeById(long id);

    List<Theme> findAll();

    List<Theme> findRankingByPeriod(LocalDate startDate, LocalDate endDate, int limit);
}
