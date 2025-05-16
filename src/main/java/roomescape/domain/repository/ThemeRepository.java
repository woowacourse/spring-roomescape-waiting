package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Theme;

public interface ThemeRepository {

    Theme save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    void deleteById(Long id);

    List<Theme> findRankByDate(LocalDate startDate, LocalDate endDate, int limit);
}
