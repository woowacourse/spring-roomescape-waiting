package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import roomescape.theme.domain.Theme;

public interface ThemeRepository {

    List<Theme> findTop10PopularThemesWithinLastWeek(LocalDate fromDate, LocalDate toDate, Pageable pageable);

    List<Theme> findAll();

    void deleteById(Long id);

    Theme save(Theme theme);

    Optional<Theme> findById(Long request);
}
