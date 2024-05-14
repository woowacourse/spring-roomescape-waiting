package roomescape.domain;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    Theme save(Theme theme);

    int deleteById(Long id);

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    boolean isNameExists(String name);

    List<Theme> findMostReservedThemesInPeriod(int period, int count);
}
