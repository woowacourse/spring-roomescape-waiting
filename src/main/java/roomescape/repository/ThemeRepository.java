package roomescape.repository;

import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    List<Theme> findAll();

    Optional<Theme> findById(long id);

    Theme save(Theme theme);

    void deleteById(long id);

    Theme update(Theme theme);

    List<Theme> findPopularThemes(Long topCount, LocalDate fromDate, LocalDate toDate);
}
