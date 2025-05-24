package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;

public interface ThemeRepository {
    Theme save(Theme theme);

    Optional<Theme> findById(Long id);
    List<Theme> findAll();
    List<Theme> findAllOrderByRank(LocalDate from, LocalDate to, int size);

    void delete(Theme theme);
}
