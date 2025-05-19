package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;

public interface ThemeRepository {
    Theme save(Theme theme);

    List<Theme> findAll();

    void deleteById(Long id);

    Optional<Theme> findById(Long id);

    List<Theme> findTopByReservationCountDesc(LocalDate fromDate, LocalDate toDate, long listNum);
}
