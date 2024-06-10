package roomescape.domain.theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    Theme save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(long id);

    boolean existsByName(Name name);

    Theme deleteById(long id);

    List<Theme> findPopular(LocalDate start, LocalDate end, int popularThemeCount);
}
