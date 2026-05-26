package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Theme;

public interface ThemeRepository {

    boolean isActiveByName(String name);

    Theme save(Theme theme);

    void update(Theme theme);

    Optional<Theme> findById(long id);

    List<Theme> findAllActiveThemes();

    List<Theme> findTop10ByReservationCount(LocalDate startDate, LocalDate endDate);

    List<Theme> findAll();
}
