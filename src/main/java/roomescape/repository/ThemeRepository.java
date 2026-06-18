package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Theme;

public interface ThemeRepository {

    List<Theme> findAll();

    Optional<Theme> findById(long id);

    Theme save(Theme theme);

    void deleteById(long id);

    List<Theme> findPopularThemes(Long topCount, LocalDate fromDate, LocalDate toDate);
}
