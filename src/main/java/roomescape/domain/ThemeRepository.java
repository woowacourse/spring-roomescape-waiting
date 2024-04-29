package roomescape.domain;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    List<Theme> findMostReservedThemesWithinDays(int day, int limit);

    Theme save(Theme theme);

    void deleteById(Long id);
}
