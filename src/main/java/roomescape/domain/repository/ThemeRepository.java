package roomescape.domain.repository;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Theme;

public interface ThemeRepository {
    Theme save(Theme theme);

    List<Theme> findAll();

    int deleteById(long id);

    Optional<Theme> findByName(String name);

    Optional<Theme> findById(@NotNull Long aLong);

    List<Theme> findByDateRangeOrderByReservationCountLimitN(LocalDate startDate, LocalDate endDate, int count);
}