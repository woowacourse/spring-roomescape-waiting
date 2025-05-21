package roomescape.theme.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import roomescape.theme.domain.Theme;

public interface ThemeRepository {

    Page<Theme> findPopularThemes(LocalDate fromDate, LocalDate toDate, Pageable pageable);

    List<Theme> findAll();

    void deleteById(Long id);

    Theme save(Theme theme);

    Optional<Theme> findById(Long request);
}
