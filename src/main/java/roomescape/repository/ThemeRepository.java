package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.entity.Theme;

public interface ThemeRepository {

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    List<Theme> findTop10ByDateBetween(LocalDate startDate, LocalDate endDate);

    boolean existsByName(String name);

    Theme save(Theme theme);

    void deleteById(Long id);
}
