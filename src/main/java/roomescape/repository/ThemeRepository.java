package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Theme;

public interface ThemeRepository {

    Optional<Theme> save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(long id);

    List<Theme> findMostReservedByDateRange(LocalDate start, LocalDate end);

    int deleteById(long id);
}
