package roomescape.theme.domain;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository {

    Long save(Theme theme);

    List<Theme> findPopularThemes(LocalDate start, LocalDate end, int popularCount);

    List<Theme> findAll();

    boolean deleteById(Long id);

    Theme findById(Long id);
}
