package roomescape.persistence;

import java.time.LocalDate;
import java.util.List;
import roomescape.model.Theme;

public interface ThemeRepository {

    boolean isDuplicatedThemeName(String name);

    List<Theme> findPopularThemesInPeriod(
            LocalDate startDate,
            LocalDate endDate,
            int size
    );

    Theme findById(Long id);
}
